package com.gesh.backend.dto;

public record AssignmentRequestSummary (
        String id,
        String patientId,
        String patientName,
        String doctorId,
        String status,         // "pending" | "accepted" | "rejected"
        String createdAt,
        String respondedAt     // can be NULL
){}
