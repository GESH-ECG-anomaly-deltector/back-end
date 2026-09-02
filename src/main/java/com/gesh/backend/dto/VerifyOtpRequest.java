package com.gesh.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "ایمیل الزامی است")
    @Email(message = "فرمت ایمیل درست نیست")
    private String email;

    @NotBlank(message = "کد تایید الزامی است")
    @Size(min = 6, max = 6, message = "کد باید ۶ رقم باشد")
    private String code;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}


