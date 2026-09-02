package com.gesh.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;

    @Column(unique = true, length = 11)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 10)
    private String nationalCode;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String role;       // "patient" | "doctor" | "admin"
    private String profileId;
    private String createdAt; //به شمسی باید تبدیل بشهTODO:

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;
}
