package com.gesh.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assignment_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {
    @Id
    private String id;
    private String patientId;
    private String doctorId;
    private String status;          // "pending" | "accepted" | "rejected"
    private String createdAt;
    private String respondedAt;     // can be NULL
}
