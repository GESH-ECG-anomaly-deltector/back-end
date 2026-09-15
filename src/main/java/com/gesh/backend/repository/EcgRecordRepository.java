package com.gesh.backend.repository;

import com.gesh.backend.model.EcgRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EcgRecordRepository extends JpaRepository<EcgRecord, String> {
    List<EcgRecord> findByPatientIdOrderByCreatedAtDesc(String patientId);
}
