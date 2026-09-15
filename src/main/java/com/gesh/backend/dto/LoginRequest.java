package com.gesh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "شماره موبایل یا کد ملی الزامی است")
    private String identifier;

    @NotBlank(message = "رمز عبور الزامی است")
    private String password;
}