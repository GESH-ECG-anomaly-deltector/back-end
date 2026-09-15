package com.gesh.backend.controller;

import com.gesh.backend.dto.CreateRecordRequest;
import com.gesh.backend.dto.DoctorReviewRequest;
import com.gesh.backend.model.EcgRecord;
import com.gesh.backend.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/patient/{patientId}")
    public List<EcgRecord> getByPatient(@PathVariable String patientId) {
        return recordService.getRecordsForPatient(patientId);
    }

    @GetMapping("/{recId}")
    public EcgRecord getById(@PathVariable String recId) {
        return recordService.getRecordById(recId);
    }

    @PostMapping
    public EcgRecord create(@Valid @RequestBody CreateRecordRequest request) {
        return recordService.createRecord(
                request.getPatientId(),
                request.getDuration(),
                request.getSource(),
                request.getSymptoms(),
                request.isNeedsDoctorReview(),
                request.getNationalCode()
        );
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public EcgRecord createFromFile(
            @RequestParam String patientId,
            @RequestParam int duration,
            @RequestParam String source,
            @RequestParam(required = false) String symptoms,
            @RequestParam boolean needsDoctorReview,
            @RequestParam(required = false) String nationalCode,
            @RequestParam MultipartFile file
    ) {
        return recordService.createRecordFromFile(
                patientId, duration, source, symptoms, needsDoctorReview, nationalCode, file
        );
    }

    @PatchMapping("/{recId}/request-review")
    public EcgRecord requestReview(@PathVariable String recId) {
        return recordService.requestDoctorReview(recId);
    }

    @PatchMapping("/{recId}/review")
    public EcgRecord submitReview(@PathVariable String recId, @Valid @RequestBody DoctorReviewRequest request) {
        return recordService.submitDoctorReview(recId, request.getDoctorId(), request.getNote(), request.isApproved());
    }
}