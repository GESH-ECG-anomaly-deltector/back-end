package com.gesh.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gesh.backend.client.MlInferenceClient;
import com.gesh.backend.model.AccountStatus;
import com.gesh.backend.model.Diagnosis;
import com.gesh.backend.model.Doctor;
import com.gesh.backend.model.DoctorNote;
import com.gesh.backend.model.EcgRecord;
import com.gesh.backend.model.Lead;
import com.gesh.backend.model.User;
import com.gesh.backend.repository.DoctorRepository;
import com.gesh.backend.repository.EcgRecordRepository;
import com.gesh.backend.repository.UserRepository;
import com.gesh.backend.util.EcgSignalParser;
import com.gesh.backend.util.JalaliDateUtil;
import com.gesh.backend.util.NationalCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class RecordService {

    private static final Logger log = LoggerFactory.getLogger(RecordService.class);

    private static final String[] LEAD_NAMES = {
            "I", "II", "III", "aVR", "aVL", "aVF",
            "V1", "V2", "V3", "V4", "V5", "V6"
    };

    private final EcgRecordRepository ecgRecordRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final MlInferenceClient mlInferenceClient;
    private final boolean fallbackOnError;
    private final Random random = new Random();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RecordService(EcgRecordRepository ecgRecordRepository, UserRepository userRepository,
                         DoctorRepository doctorRepository, MlInferenceClient mlInferenceClient,
                         @Value("${ml.service.fallback-on-error:true}") boolean fallbackOnError) {
        this.ecgRecordRepository = ecgRecordRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.mlInferenceClient = mlInferenceClient;
        this.fallbackOnError = fallbackOnError;
    }

    public List<EcgRecord> getRecordsForPatient(String patientId) {
        return ecgRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public EcgRecord getRecordById(String recId) {
        return ecgRecordRepository.findById(recId)
                .orElseThrow(() -> new IllegalArgumentException("رکورد یافت نشد"));
    }

    public EcgRecord createRecord(String patientId, int duration, String source,
                                  String symptoms, boolean needsDoctorReview, String nationalCode) {
        ensureNationalCode(patientId, nationalCode);
        return buildAndSaveRecord(patientId, duration, source, symptoms, needsDoctorReview, fakeAnalyze(), List.of());
    }

    public EcgRecord createRecordFromFile(String patientId, int duration, String source,
                                          String symptoms, boolean needsDoctorReview,
                                          String nationalCode, MultipartFile file) {
        ensureNationalCode(patientId, nationalCode);

        List<Diagnosis> diagnoses;
        List<Lead> leads;
        try (InputStream in = file.getInputStream()) {
            float[][] signal = parseByExtension(file.getOriginalFilename(), in);
            leads = buildLeadsFromSignal(signal);
            diagnoses = mlInferenceClient.classify(signal);
            if (diagnoses == null || diagnoses.isEmpty()) {
                throw new IllegalStateException("سرویس مدل خروجی خالی برگردوند");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("خواندن فایل نوار قلب با خطا مواجه شد", e);
        } catch (Exception e) {
            if (!fallbackOnError) {
                throw new IllegalStateException("سرویس تحلیل مدل در دسترس نیست", e);
            }
            log.warn("سرویس مدل در دسترس نبود، موقتاً از تحلیل جایگزین استفاده شد: {}", e.getMessage());
            diagnoses = fakeAnalyze();
            leads = List.of();
        }

        return buildAndSaveRecord(patientId, duration, source, symptoms, needsDoctorReview, diagnoses, leads);
    }

    private float[][] parseByExtension(String filename, InputStream in) throws IOException {
        String lower = filename != null ? filename.toLowerCase() : "";
        if (lower.endsWith(".npy")) {
            return EcgSignalParser.parseNpy(in);
        }
        return EcgSignalParser.parseCsv(in);
    }

    private List<Lead> buildLeadsFromSignal(float[][] signal) {
        List<Lead> leads = new ArrayList<>();
        for (int i = 0; i < signal.length; i++) {
            String name = i < LEAD_NAMES.length ? LEAD_NAMES[i] : "Lead " + (i + 1);
            leads.add(new Lead(name, serializeGraph(signal[i])));
        }
        return leads;
    }

    private String serializeGraph(float[] samples) {
        try {
            return objectMapper.writeValueAsString(samples);
        } catch (JsonProcessingException e) {
            log.warn("سریالایز کردن سیگنال لید با خطا مواجه شد: {}", e.getMessage());
            return "[]";
        }
    }

    private EcgRecord buildAndSaveRecord(String patientId, int duration, String source, String symptoms,
                                         boolean needsDoctorReview, List<Diagnosis> diagnoses, List<Lead> leads) {
        String recId = String.valueOf(1000 + random.nextInt(9000));
        Instant createdAt = Instant.now();
        String lastRecDate = JalaliDateUtil.formatDateTime(createdAt);

        String primaryDiagnosisCode = diagnoses.get(0).getCode();

        int sampleCount = leads.isEmpty() ? 0 : sampleCountOf(leads.get(0));
        int sampleRate = (sampleCount > 0 && duration > 0) ? sampleCount / duration : 100;

        EcgRecord record = new EcgRecord(
                recId,
                patientId,
                lastRecDate,
                createdAt,
                leads.isEmpty() ? 12 : leads.size(),
                duration,
                sampleRate,
                88 + random.nextInt(10),
                65 + random.nextInt(30),
                needsDoctorReview ? "pending" : "model-only",
                primaryDiagnosisCode,
                diagnoses,
                leads,
                null,
                symptoms,
                source
        );

        return ecgRecordRepository.save(record);
    }

    private int sampleCountOf(Lead lead) {
        try {
            return objectMapper.readValue(lead.getGraph(), float[].class).length;
        } catch (Exception e) {
            return 0;
        }
    }


    private void ensureNationalCode(String patientId, String nationalCode) {
        User owner = userRepository.findByProfileId(patientId)
                .filter(u -> "patient".equals(u.getRole()))
                .orElseThrow(() -> new IllegalArgumentException("کاربر مربوط به این بیمار یافت نشد"));

        if (owner.getNationalCode() != null && !owner.getNationalCode().isBlank()) {
            return;
        }

        if (nationalCode == null || nationalCode.isBlank()) {
            throw new IllegalArgumentException("برای ثبت اولین نوار قلب، وارد کردن کد ملی الزامی است");
        }
        if (!NationalCodeUtil.isValid(nationalCode)) {
            throw new IllegalArgumentException("کد ملی وارد شده معتبر نیست");
        }

        boolean alreadyTaken = userRepository.findByNationalCode(nationalCode).isPresent();
        if (alreadyTaken) {
            throw new IllegalArgumentException("این کد ملی قبلاً برای کاربر دیگری ثبت شده است");
        }

        owner.setNationalCode(nationalCode);
        owner.setStatus(AccountStatus.ACTIVE);
        userRepository.save(owner);
    }

    public EcgRecord requestDoctorReview(String recId) {
        EcgRecord record = getRecordById(recId);
        record.setStatus("pending");
        return ecgRecordRepository.save(record);
    }

    public EcgRecord submitDoctorReview(String recId, String doctorId, String note, boolean approved) {
        EcgRecord record = getRecordById(recId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("پزشک یافت نشد"));

        String noteText = (note == null || note.isBlank())
                ? (approved ? "پزشک تشخیص مدل را تایید کرد." : "پزشک با تشخیص مدل مخالفت کرد.")
                : note.trim();

        record.setDoctorNote(new DoctorNote(
                doctor.getName(),
                JalaliDateUtil.formatDateTime(LocalDateTime.now()),
                noteText
        ));
        record.setStatus("reviewed");
        return ecgRecordRepository.save(record);
    }

    private List<Diagnosis> fakeAnalyze() {
        return List.of(
                new Diagnosis("NSR", "ریتم سینوسی طبیعی", 85 + random.nextInt(10), "mock"),
                new Diagnosis("SB", "برادی‌کاردی خفیف", 2 + random.nextInt(8), "mock"),
                new Diagnosis("AF", "فیبریلاسیون دهلیزی", 1 + random.nextInt(5), "mock")
        );
    }
}