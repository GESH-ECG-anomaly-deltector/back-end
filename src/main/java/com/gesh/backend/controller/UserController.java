package com.gesh.backend.controller;

import com.gesh.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/by-profile/{profileId}")
    public Map<String, String> getNationalCodeByProfileId(@PathVariable String profileId) {
        var user = userRepository.findByProfileId(profileId)
                .orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد"));

        return Map.of("nationalCode", user.getNationalCode() != null ? user.getNationalCode() : "");
    }
}
