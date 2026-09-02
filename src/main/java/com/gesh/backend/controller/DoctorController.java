package com.gesh.backend.controller;

import com.gesh.backend.model.Doctor;
import com.gesh.backend.model.Patient;
import com.gesh.backend.repository.DoctorRepository;
import com.gesh.backend.repository.PatientRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public DoctorController(DoctorRepository doctorRepository, PatientRepository patientRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping("/{id}")
    public Doctor getById(@PathVariable String id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("پزشک یافت نشد"));
    }

    @GetMapping("/{id}/patients")
    public List<Patient> getPatients(@PathVariable String id) {
        return patientRepository.findByAssignedDoctorId(id);
    }
}
