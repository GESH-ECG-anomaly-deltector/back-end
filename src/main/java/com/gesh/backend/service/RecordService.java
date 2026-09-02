package com.gesh.backend.service;

import com.gesh.backend.model.AccountStatus;
import com.gesh.backend.model.Diagnosis;
import com.gesh.backend.model.EcgRecord;
import com.gesh.backend.model.Lead;
import com.gesh.backend.model.User;
import com.gesh.backend.repository.EcgRecordRepository;
import com.gesh.backend.repository.UserRepository;
import com.gesh.backend.util.NationalCodeUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class RecordService {

    private final EcgRecordRepository ecgRecordRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public RecordService(EcgRecordRepository ecgRecordRepository, UserRepository userRepository) {
        this.ecgRecordRepository = ecgRecordRepository;
        this.userRepository = userRepository;
    }

    public List<EcgRecord> getRecordsForPatient(String patientId) {
        return ecgRecordRepository.findByPatientId(patientId);
    }

    public EcgRecord getRecordById(String recId) {
        return ecgRecordRepository.findById(recId)
                .orElseThrow(() -> new IllegalArgumentException("رکورد یافت نشد"));
    }


    public EcgRecord createRecord(String patientId, int duration, String source,
                                   String symptoms, boolean needsDoctorReview, String nationalCode) {

        ensureNationalCode(patientId, nationalCode);

        String recId = String.valueOf(1000 + random.nextInt(9000));
        String persianDate = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm")
                .format(LocalDateTime.now());
        String createdAt = LocalDateTime.now().toString();

        List<Diagnosis> diagnoses = fakeAnalyze();
        String primaryDiagnosisCode = diagnoses.get(0).getCode();

        List<Lead> leads = List.of(
                new Lead("Lead I", ""),
                new Lead("Lead II", ""),
                new Lead("Lead III", ""),
                new Lead("Lead aVF", ""),
                new Lead("Lead aVL", ""),
                new Lead("Lead aVR", "")
        );

        EcgRecord record = new EcgRecord(
                recId,
                patientId,
                persianDate,
                createdAt,
                12,
                duration,
                500,
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

    private List<Diagnosis> fakeAnalyze() {
        return List.of(
                new Diagnosis("NSR", "ریتم سینوسی طبیعی", 85 + random.nextInt(10)),
                new Diagnosis("SB", "برادی‌کاردی خفیف", 2 + random.nextInt(8)),
                new Diagnosis("AF", "فیبریلاسیون دهلیزی", 1 + random.nextInt(5))
        );
    }
}
