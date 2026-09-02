package com.gesh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRecordRequest {

    @NotBlank(message = "شناسه‌ی بیمار الزامی است")
    private String patientId;

    @NotNull(message = "مدت زمان سیگنال الزامی است")
    private Integer duration;

    @NotBlank(message = "منبع سیگنال الزامی است")
    private String source; // "12-lead" | "device"

    private String symptoms; // Optional

    private boolean needsDoctorReview = true;

    private String nationalCode;
}
