package com.gesh.backend.controller;

import com.gesh.backend.dto.AuthResponse;
import com.gesh.backend.dto.EmailLoginRequest;
import com.gesh.backend.dto.LoginRequest;
import com.gesh.backend.dto.SendOtpRequest;
import com.gesh.backend.dto.SignupRequest;
import com.gesh.backend.dto.SignupWithEmailRequest;
import com.gesh.backend.dto.VerifyOtpRequest;
import com.gesh.backend.service.AuthService;
import com.gesh.backend.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/login/email")
    public AuthResponse loginWithEmailPassword(@Valid @RequestBody EmailLoginRequest request) {
        return authService.loginWithEmailPassword(request);
    }

    @PostMapping("/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/signup/email")
    public AuthResponse signupWithEmail(@Valid @RequestBody SignupWithEmailRequest request) {
        return authService.signupWithEmail(request);
    }

    @PostMapping("/send-otp")
    public void sendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtp(request.getEmail());
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        if (!otpService.isCodeValid(request.getEmail(), request.getCode())) {
            throw new IllegalArgumentException("کد وارد شده اشتباه یا منقضی شده است");
        }

        AuthResponse response = authService.loginWithEmail(request.getEmail());
        otpService.consume(request.getEmail());
        return response;
    }
}