package com.gesh.backend.controller;

import com.gesh.backend.dto.AssignmentRequestSummary;
import com.gesh.backend.model.AssignmentRequest;
import com.gesh.backend.model.Doctor;
import com.gesh.backend.service.AssignmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/doctors/approved")
    public List<Doctor> getApprovedDoctors() {
        return assignmentService.getApprovedDoctors();
    }

    @PostMapping("/patients/{patientId}/request-doctor/{doctorId}")
    public AssignmentRequest requestDoctor(
            @PathVariable String patientId,
            @PathVariable String doctorId) {
        return assignmentService.requestDoctor(patientId, doctorId);
    }

    @GetMapping("/doctors/{doctorId}/assignment-requests")
    public List<AssignmentRequestSummary> getPendingRequests(@PathVariable String doctorId) {
        return assignmentService.getPendingRequestsForDoctor(doctorId);
    }

    @PostMapping("/doctors/{doctorId}/assignment-requests/{requestId}/accept")
    public AssignmentRequest accept(
            @PathVariable String doctorId,
            @PathVariable String requestId) {
        return assignmentService.acceptRequest(requestId, doctorId);
    }

    @PostMapping("/doctors/{doctorId}/assignment-requests/{requestId}/reject")
    public AssignmentRequest reject(
            @PathVariable String doctorId,
            @PathVariable String requestId) {
        return assignmentService.rejectRequest(requestId, doctorId);
    }
}