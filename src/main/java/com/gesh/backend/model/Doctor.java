package com.gesh.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id
    private String id;
    private String name;
    private String email;
    private String specialty;
    private String medicalCode;
    private String first2letters;
    private String status;   // "pending" | "approved" | "rejected"
}
