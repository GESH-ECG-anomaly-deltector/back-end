package com.gesh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorReviewRequest {

    @NotBlank(message = "شناسه پزشک الزامی است")
    private String doctorId;

    private String note;

    private boolean approved = true;
}