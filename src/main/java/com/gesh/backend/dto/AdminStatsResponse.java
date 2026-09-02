package com.gesh.backend.dto;

public record AdminStatsResponse(
        long todayRecords,
        String todayRecordsDelta,

        double serverLoad,
        String serverLoadDelta,

        double avgInferenceTime,
        String avgInferenceTimeDelta,

        double modelAccuracy,
        String modelAccuracyDelta
) {}