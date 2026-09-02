package com.gesh.backend.repository;

import com.gesh.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, String> {
    List<Patient> findByAssignedDoctorId(String doctorId);
}
