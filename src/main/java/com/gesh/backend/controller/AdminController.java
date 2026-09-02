package com.gesh.backend.controller;

import com.gesh.backend.dto.UserSummaryResponse;
import com.gesh.backend.model.Doctor;
import com.gesh.backend.service.AdminService;
import org.springframework.web.bind.annotation.*;
import com.gesh.backend.dto.AdminStatsResponse;

import java.util.List;
//TODO: Spring Security

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) { this.adminService = adminService; }

    @GetMapping("/users")
    public List<UserSummaryResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/doctors/pending")
    public List<Doctor> getPendingDoctors() { return adminService.getPendingDoctors(); }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() { return adminService.getStats(); }

    @PostMapping("/doctors/{doctorId}/approve")
    public Doctor approve(@PathVariable String doctorId) { return adminService.approveDoctor(doctorId); }

    @PostMapping("/doctors/{doctorId}/reject")
    public Doctor reject(@PathVariable String doctorId) { return adminService.rejectDoctor(doctorId); }
}