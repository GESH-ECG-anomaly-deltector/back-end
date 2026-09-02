package com.gesh.backend.repository;

import com.gesh.backend.model.AssignmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRequestRepository extends JpaRepository<AssignmentRequest, String> {
    List<AssignmentRequest> findByDoctorIdAndStatus(String doctorId, String status);
    boolean existsByPatientIdAndDoctorIdAndStatus(String patientId, String doctorId, String status);
}
