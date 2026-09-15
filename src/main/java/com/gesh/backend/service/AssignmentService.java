package com.gesh.backend.service;

import com.gesh.backend.dto.AssignmentRequestSummary;
import com.gesh.backend.model.AssignmentRequest;
import com.gesh.backend.model.Doctor;
import com.gesh.backend.model.Patient;
import com.gesh.backend.repository.AssignmentRequestRepository;
import com.gesh.backend.repository.DoctorRepository;
import com.gesh.backend.repository.PatientRepository;
import com.gesh.backend.util.JalaliDateUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AssignmentService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AssignmentRequestRepository assignmentRequestRepository;

    public AssignmentService(PatientRepository patientRepository, DoctorRepository doctorRepository,
                             AssignmentRequestRepository assignmentRequestRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.assignmentRequestRepository = assignmentRequestRepository;
    }

    public AssignmentRequest requestDoctor(String patientId, String doctorId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("بیمار یافت نشد"));

        if (patient.getAssignedDoctorId() != null) {
            throw new IllegalArgumentException("شما قبلاً به یک پزشک متصل هستید");
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("پزشک یافت نشد"));

        if (!"approved".equals(doctor.getStatus())) {
            throw new IllegalArgumentException("این پزشک هنوز توسط ادمین تایید نشده است");
        }

        boolean alreadyPending = assignmentRequestRepository
                .existsByPatientIdAndDoctorIdAndStatus(patientId, doctorId, "pending");
        if (alreadyPending) {
            throw new IllegalArgumentException("درخواست شما قبلاً برای این پزشک ارسال شده و در انتظار پاسخ است");
        }

        AssignmentRequest request = new AssignmentRequest(
                "ar-" + UUID.randomUUID().toString().substring(0, 8),
                patientId,
                doctorId,
                "pending",
                JalaliDateUtil.formatDateTimeCompact(LocalDateTime.now()),
                null
        );
        return assignmentRequestRepository.save(request);
    }

    public List<AssignmentRequestSummary> getPendingRequestsForDoctor(String doctorId) {
        return assignmentRequestRepository.findByDoctorIdAndStatus(doctorId, "pending").stream()
                .map(this::toSummary)
                .toList();
    }

    private AssignmentRequestSummary toSummary(AssignmentRequest request) {
        String patientName = patientRepository.findById(request.getPatientId())
                .map(Patient::getName)
                .orElse("undefined");
        return new AssignmentRequestSummary(
                request.getId(),
                request.getPatientId(),
                patientName,
                request.getDoctorId(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getRespondedAt()
        );
    }


    public AssignmentRequest acceptRequest(String requestId, String doctorId) {
        AssignmentRequest request = findRequest(requestId);

        if (!request.getDoctorId().equals(doctorId)) {
            throw new IllegalArgumentException("این درخواست متعلق به شما نیست");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("این درخواست قبلاً پاسخ داده شده است");
        }

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("بیمار یافت نشد"));

        patient.setAssignedDoctorId(doctorId);
        patientRepository.save(patient);

        request.setStatus("accepted");
        request.setRespondedAt(JalaliDateUtil.formatDateTimeCompact(LocalDateTime.now()));
        return assignmentRequestRepository.save(request);
    }

    public AssignmentRequest rejectRequest(String requestId, String doctorId) {
        AssignmentRequest request = findRequest(requestId);

        if (!request.getDoctorId().equals(doctorId)) {
            throw new IllegalArgumentException("این درخواست متعلق به شما نیست");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("این درخواست قبلاً پاسخ داده شده است");
        }

        request.setStatus("rejected");
        request.setRespondedAt(JalaliDateUtil.formatDateTimeCompact(LocalDateTime.now()));
        return assignmentRequestRepository.save(request);
    }

    public List<Doctor> getApprovedDoctors() {
        return doctorRepository.findByStatus("approved");
    }

    private AssignmentRequest findRequest(String requestId) {
        return assignmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("درخواست یافت نشد"));
    }
}