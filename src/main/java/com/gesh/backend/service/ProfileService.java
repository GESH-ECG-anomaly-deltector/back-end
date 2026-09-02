package com.gesh.backend.service;

import com.gesh.backend.model.User;
import com.gesh.backend.repository.AdminRepository;
import com.gesh.backend.repository.DoctorRepository;
import com.gesh.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;

    public ProfileService(PatientRepository patientRepository, DoctorRepository doctorRepository,
                           AdminRepository adminRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.adminRepository = adminRepository;
    }

    public Object resolveProfile(User user) {
        if ("patient".equals(user.getRole())) {
            return patientRepository.findById(user.getProfileId()).orElse(null);
        }
        if ("admin".equals(user.getRole())) {
            return adminRepository.findById(user.getProfileId()).orElse(null);
        }
        return doctorRepository.findById(user.getProfileId()).orElse(null);
    }
}
