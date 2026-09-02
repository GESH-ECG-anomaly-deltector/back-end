package com.gesh.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SendOtpRequest {

    @NotBlank(message = "ایمیل الزامی است")
    @Email(message = "فرمت ایمیل نامعتبر است")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}