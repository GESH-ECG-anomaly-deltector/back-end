package com.gesh.backend.dto;

import com.gesh.backend.model.AccountStatus;

public record UserSummaryResponse (
      String id,
      String phone,
      String email,
      String nationalCode,
      String role,
      String profileId,
      String createdAt,
      AccountStatus status,

      String name,
      String medicalCode
){}
