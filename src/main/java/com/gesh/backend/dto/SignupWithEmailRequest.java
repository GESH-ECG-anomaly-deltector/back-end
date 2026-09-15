package com.gesh.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupWithEmailRequest {

    @NotBlank(message = "نام و نام خانوادگی الزامی است")
    @Pattern(regexp = "^[\\u0600-\\u06FF\\s\\u200C]+$", message = "نام باید فقط با حروف فارسی نوشته شود")
    private String name;

    @NotBlank(message = "ایمیل الزامی است")
    @Email(message = "فرمت ایمیل درست نیست")
    private String email;

    @NotBlank(message = "شماره موبایل الزامی است")
    private String phone;

    @NotBlank(message = "کد تایید الزامی است")
    @Size(min = 6, max = 6, message = "کد باید ۶ رقم باشد")
    private String code;

    @NotBlank(message = "رمز عبور الزامی است")
    private String password;

    @NotBlank(message = "نقش کاربر (patient/doctor) الزامی است")
    private String role;

    private String medicalCode;
}