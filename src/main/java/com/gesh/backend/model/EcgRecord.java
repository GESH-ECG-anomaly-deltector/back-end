package com.gesh.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "ecg_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcgRecord {
    @Id
    private String recId;
    private String patientId;
    private String lastRecDate;
    private Instant createdAt;
    private int leadCount;
    private int duration;
    private int sampleRate;
    private int signalQuality;
    private int bpm;
    private String status;               // "processing" | "pending" | "reviewed"
    private String primaryDiagnosisCode;

    @ElementCollection
    @CollectionTable(name = "ecg_record_diagnoses", joinColumns = @JoinColumn(name = "record_id"))
    @OrderColumn(name = "position")
    private List<Diagnosis> diagnoses;

    @ElementCollection
    @CollectionTable(name = "ecg_record_leads", joinColumns = @JoinColumn(name = "record_id"))
    @OrderColumn(name = "position")
    private List<Lead> leads;

    @Embedded
    private DoctorNote doctorNote;        // can be NULL

    @Column(columnDefinition = "TEXT")
    private String symptoms;              // Optional
    private String source;                // "12-lead" | "device"
}