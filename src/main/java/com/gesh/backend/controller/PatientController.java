package com.gesh.backend.controller;

import com.gesh.backend.model.Patient;
import com.gesh.backend.repository.PatientRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository patientRepository;

    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @GetMapping("/{id}")
    public Patient getById(@PathVariable String id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("بیمار یافت نشد"));
    }

    @PutMapping("/{id}")
    public Patient update(@PathVariable String id, @RequestBody Patient updatedPatient) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("بیمار یافت نشد"));

        existing.setName(updatedPatient.getName());
        existing.setAge(updatedPatient.getAge());
        existing.setGender(updatedPatient.getGender());
        existing.setBloodType(updatedPatient.getBloodType());
        existing.setEmail(updatedPatient.getEmail());
        existing.setCity(updatedPatient.getCity());

        return patientRepository.save(existing);
    }
}
