package com.gesh.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    @Id
    private String id;
    private String name;
    private String email;
    private String patientCode;
    private String first2letters;
    private int age;
    private String gender;
    private String assignedDoctorId;  // can be NULL.
    private String bloodType;
    private String city;
    private String riskLevel; // "low" | "medium" | "high"
}
