package com.gesh.backend.service;

import com.gesh.backend.dto.UserSummaryResponse;
import com.gesh.backend.model.AccountStatus;
import com.gesh.backend.model.Doctor;
import com.gesh.backend.model.Patient;
import com.gesh.backend.model.User;
import com.gesh.backend.repository.DoctorRepository;
import com.gesh.backend.repository.EcgRecordRepository;
import com.gesh.backend.repository.UserRepository;
import com.gesh.backend.dto.AdminStatsResponse;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;


@Service
public class AdminService {

    private final DoctorRepository doctorRepository;
    private final EcgRecordRepository ecgRecordRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    public AdminService(DoctorRepository doctorRepository, EcgRecordRepository ecgRecordRepository,
                        UserRepository userRepository, ProfileService profileService) {
        this.doctorRepository = doctorRepository;
        this.ecgRecordRepository = ecgRecordRepository;
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    private UserSummaryResponse toSummary(User user) {
        String name = null;
        String medicalCode = null;
        String doctorApprovalStatus = null;

        Object profile = profileService.resolveProfile(user);

        if (profile instanceof Doctor doctor) {
            name = doctor.getName();
            medicalCode = doctor.getMedicalCode();
            doctorApprovalStatus = doctor.getStatus();
        } else if (profile instanceof Patient patient) {
            name = patient.getName();
        }

        return new UserSummaryResponse(
                user.getId(),
                user.getPhone(),
                user.getEmail(),
                user.getNationalCode(),
                user.getRole(),
                user.getProfileId(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getStatus(),
                doctorApprovalStatus,
                name,
                medicalCode
        );
    }

    public UserSummaryResponse setUserActive(String userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد"));

        if ("doctor".equals(user.getRole())) {
            Doctor doctor = doctorRepository.findById(user.getProfileId())
                    .orElseThrow(() -> new IllegalArgumentException("پروفایل پزشک یافت نشد"));
            doctor.setStatus(active ? "approved" : "rejected");
            doctorRepository.save(doctor);
        } else {
            user.setStatus(active ? AccountStatus.ACTIVE : AccountStatus.INACTIVE);
            userRepository.save(user);
        }

        return toSummary(user);
    }

    public List<Doctor> getPendingDoctors() {
        return doctorRepository.findByStatus("pending");
    }

    public Doctor approveDoctor(String doctorId) {
        Doctor doctor = findDoctor(doctorId);
        if (!"pending".equals(doctor.getStatus())) {
            throw new IllegalArgumentException("این پزشک در وضعیت انتظار تایید نیست");
        }
        doctor.setStatus("approved");
        return doctorRepository.save(doctor);
    }

    public Doctor rejectDoctor(String doctorId) {
        Doctor doctor = findDoctor(doctorId);
        if (!"pending".equals(doctor.getStatus())) {
            throw new IllegalArgumentException("این پزشک در وضعیت انتظار تایید نیست");
        }
        doctor.setStatus("rejected");
        return doctorRepository.save(doctor);
    }

    private Doctor findDoctor(String doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("پزشک یافت نشد"));
    }

    public AdminStatsResponse getStats() {
        LocalDate today = LocalDate.now();
        long todayRecords = ecgRecordRepository.findAll().stream()
                .filter(ecgRecord ->
                        ecgRecord.getCreatedAt() != null &&
                                LocalDate.ofInstant(ecgRecord.getCreatedAt(), ZoneId.systemDefault()).equals(today)
                ).count();

        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getCpuLoad();

        double serverLoad = cpuLoad >= 0
                ? Math.round( cpuLoad * 1000.0 ) / 10.0
                : 0;

        double avgInferenceTime = 1.3;
        double modelAccuracy = 94.2;

        return new AdminStatsResponse(
                todayRecords,
                "+0",
                serverLoad,
                "پایدار",
                avgInferenceTime,
                "-0.2",
                modelAccuracy,
                "+0.8"
        );
    }
}