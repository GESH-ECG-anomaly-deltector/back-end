package com.gesh.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailLoginRequest {

    @NotBlank(message = "ایمیل الزامی است")
    @Email(message = "فرمت ایمیل درست نیست")
    private String email;

    @NotBlank(message = "رمز عبور الزامی است")
    private String password;
}