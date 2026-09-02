package com.gesh.backend.service;

import com.gesh.backend.dto.AuthResponse;
import com.gesh.backend.dto.LoginRequest;
import com.gesh.backend.dto.SignupRequest;
import com.gesh.backend.dto.SignupWithEmailRequest;
import com.gesh.backend.model.AccountStatus;
import com.gesh.backend.model.Doctor;
import com.gesh.backend.model.Patient;
import com.gesh.backend.model.User;
import com.gesh.backend.repository.DoctorRepository;
import com.gesh.backend.repository.PatientRepository;
import com.gesh.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ProfileService profileService;
    private final OtpService otpService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, PatientRepository patientRepository,
                        DoctorRepository doctorRepository, ProfileService profileService,
                        OtpService otpService) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.profileService = profileService;
        this.otpService = otpService;
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier();

        User user = userRepository.findByPhoneOrNationalCode(identifier, identifier)
                .orElseThrow(() -> new IllegalArgumentException("شماره موبایل / کد ملی یا رمز عبور اشتباه است"));

        if (!matchesPassword(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("شماره موبایل / کد ملی یا رمز عبور اشتباه است");
        }

        Object profile = profileService.resolveProfile(user);
        String token = generateFakeToken(user.getId());

        return new AuthResponse(token, user.getId(), user.getRole(), profile);
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("این شماره موبایل قبلاً ثبت شده است");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("این ایمیل قبلاً ثبت شده است");
        }

        return createUserAndProfile(
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                request.getPassword(),
                request.getRole(),
                request.getMedicalCode()
        );
    }

    public AuthResponse signupWithEmail(SignupWithEmailRequest request) {
        boolean codeValid = otpService.verifyOtp(request.getEmail(), request.getCode());
        if (!codeValid) {
            throw new IllegalArgumentException("کد وارد شده اشتباه یا منقضی شده است");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("این ایمیل قبلاً ثبت شده است");
        }

        return createUserAndProfile(
                request.getName(),
                null,
                request.getEmail(),
                request.getPassword(),
                request.getRole(),
                request.getMedicalCode()
        );
    }

    private AuthResponse createUserAndProfile(String name, String phone, String email,
                                               String rawPassword, String role, String medicalCode) {
        String newUserId = "u" + UUID.randomUUID().toString().substring(0, 8);
        String newProfileId;
        Object profile;

        if ("doctor".equals(role)) {
            newProfileId = "d" + UUID.randomUUID().toString().substring(0, 8);
            Doctor doctor = new Doctor(
                    newProfileId,
                    name,
                    email,
                    "",
                    medicalCode,
                    firstTwoLetters(name),
                    "pending"
            );
            doctorRepository.save(doctor);
            profile = doctor;
        } else {
            newProfileId = "p" + UUID.randomUUID().toString().substring(0, 8);
            String patientCode = "PT-" + (200 + patientRepository.count() + 1);
            Patient patient = new Patient(
                    newProfileId,
                    name,
                    email,
                    patientCode,
                    firstTwoLetters(name),
                    0,
                    "",
                    null,
                    "",
                    "",
                    "low"
            );
            patientRepository.save(patient);
            profile = patient;
        }

        AccountStatus status = "patient".equals(role) ? AccountStatus.PENDING_NATIONAL_CODE : AccountStatus.ACTIVE;

        User newUser = new User(
                newUserId,
                phone,
                passwordEncoder.encode(rawPassword),
                null,
                email,
                role,
                newProfileId,
                LocalDateTime.now().toString(),
                status
        );

        userRepository.save(newUser);

        String token = generateFakeToken(newUserId);
        return new AuthResponse(token, newUserId, role, profile);
    }

    public AuthResponse loginWithEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("کاربری با این ایمیل یافت نشد"));

        Object profile = profileService.resolveProfile(user);
        String token = generateFakeToken(user.getId());

        return new AuthResponse(token, user.getId(), user.getRole(), profile);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (storedPassword != null && storedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    private String firstTwoLetters(String name) {
        String trimmed = name.trim();
        return trimmed.length() >= 2 ? trimmed.substring(0, 2) : trimmed;
    }

    //TODO: Spring Security + JWT
    private String generateFakeToken(String userId) {
        String raw = userId + ":" + UUID.randomUUID();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
