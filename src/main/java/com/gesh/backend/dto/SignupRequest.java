package com.gesh.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class SignupRequest {

    @NotBlank(message = "نام و نام خانوادگی الزامی است")
    @Pattern(regexp = "^[\\u0600-\\u06FF\\s\\u200C]+$", message = "نام باید فقط با حروف فارسی نوشته شود")
    private String name;

    @NotBlank(message = "شماره موبایل الزامی است")
    @Pattern(regexp = "^09\\d{9}$", message = "شماره موبایل باید ۱۱ رقم و با ۰۹ شروع شود")
    private String phone;

    @Email(message = "فرمت ایمیل درست نیست")
    private String email;

    @NotBlank(message = "رمز عبور الزامی است")
    private String password;

    @NotBlank(message = "نقش کاربر (patient/doctor) الزامی است")
    private String role;

    private String medicalCode;
}
