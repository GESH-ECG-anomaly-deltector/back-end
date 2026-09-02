package com.gesh.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diagnosis {
    private String code;
    @Column(columnDefinition = "TEXT")
    private String label;
    private int confidence;
}
