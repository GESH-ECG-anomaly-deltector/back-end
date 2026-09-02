package com.gesh.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 11)
    private String phone;

    @Column(nullable = false)
    private String password; //TODO: must be hashed

    @Column(nullable = false)
    private String role;  // "patient" | "doctor"

    @Column(nullable = false, unique = true)
    private String verificationCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private String email;

    @Column(nullable = false)
    private boolean isVerified = false;
}