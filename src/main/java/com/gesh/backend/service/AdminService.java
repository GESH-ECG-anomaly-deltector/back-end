package com.gesh.backend.service;

import com.gesh.backend.dto.UserSummaryResponse;
import com.gesh.backend.model.Doctor;
import com.gesh.backend.model.Patient;
import com.gesh.backend.repository.DoctorRepository;
import com.gesh.backend.repository.EcgRecordRepository;
import com.gesh.backend.repository.UserRepository;
import com.gesh.backend.dto.AdminStatsResponse;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
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
                .map(user -> {
                    String name = null;
                    String medicalCode = null;

                    Object profile = profileService.resolveProfile(user);

                    if (profile instanceof Doctor doctor) {
                        name = doctor.getName();
                        medicalCode = doctor.getMedicalCode();
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
                            user.getStatus(),
                            name,
                            medicalCode
                    );
                }).toList();
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
                            ecgRecord.getCreatedAt().startsWith(today.toString())
                        ).count();

        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getCpuLoad();

        double serverLoad = cpuLoad >= 0
                ? Math.round( cpuLoad * 1000.0 ) / 10.0
                : 0;

        //placeholders:
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
