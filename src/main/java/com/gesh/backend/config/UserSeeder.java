package com.gesh.backend.config;

import com.gesh.backend.model.AccountStatus;
import com.gesh.backend.model.User;
import com.gesh.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        seedIfMissing(demoUser("u1", "09123456789", "test1234", "1234567891",
                "zeinab.janati@example.com", "patient", "p1", "1405/02/14", AccountStatus.ACTIVE));

        seedIfMissing(demoUser("u2", "09121112233", "test1234", "0499370899",
                "amir.hosseini@example.com", "patient", "p2", "1405/01/05", AccountStatus.ACTIVE));

        seedIfMissing(demoUser("u3", "09354445566", "test1234", null,
                "sara.mohammadi@example.com", "doctor", "d1", "1404/11/02", AccountStatus.ACTIVE));

        seedIfMissing(demoUser("u4", "09127778899", "test1234", null,
                "ali.rezaei@example.com", "doctor", "d2", "1405/05/09", AccountStatus.ACTIVE));

        seedIfMissing(demoUser("u5", "09120000000", "admin1234", null,
                "admin@gesh.ir", "admin", "a1", "1403/06/01", AccountStatus.ACTIVE));
    }

    private void seedIfMissing(User user) {
        if (!userRepository.existsById(user.getId())) {
            userRepository.save(user);
        }
    }

    private User demoUser(String id, String phone, String rawPassword, String nationalCode,
                           String email, String role, String profileId, String createdAt,
                           AccountStatus status) {
        User user = new User();
        user.setId(id);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNationalCode(nationalCode);
        user.setEmail(email);
        user.setRole(role);
        user.setProfileId(profileId);
        user.setCreatedAt(createdAt);
        user.setStatus(status);
        return user;
    }
}
