package com.gesh.backend.repository;

import com.gesh.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, String> {
    List<Doctor> findByStatus(String status);
}
