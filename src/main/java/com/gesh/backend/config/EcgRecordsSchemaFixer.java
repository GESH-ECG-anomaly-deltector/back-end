package com.gesh.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EcgRecordsSchemaFixer {

    private static final Logger log = LoggerFactory.getLogger(EcgRecordsSchemaFixer.class);

    private final JdbcTemplate jdbcTemplate;

    public EcgRecordsSchemaFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fixCreatedAtColumnTypeIfNeeded() {
        String currentType;
        try {
            currentType = jdbcTemplate.queryForObject(
                    "SELECT data_type FROM information_schema.columns " +
                            "WHERE table_name = 'ecg_records' AND column_name = 'created_at'",
                    String.class
            );
        } catch (Exception e) {
            log.info("جدول ecg_records هنوز پیدا نشد (احتمالاً اولین اجرا)، نیازی به فیکس ستون نیست.");
            return;
        }

        if (currentType != null && currentType.toLowerCase().contains("timestamp")) {
            log.info("ستون ecg_records.created_at از قبل timestamp هست، نیازی به تغییر نیست.");
            return;
        }

        log.warn("ستون ecg_records.created_at هنوز از نوع «{}» هست - در حال تبدیل خودکار به timestamptz ...", currentType);
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE ecg_records " +
                            "ALTER COLUMN created_at TYPE timestamptz " +
                            "USING created_at::timestamptz"
            );
            log.warn("تبدیل ستون created_at با موفقیت انجام شد. تاریخچه‌ی رکوردها و نمودارها باید دوباره کار کنن.");
        } catch (Exception e) {
            log.error(
                    "تبدیل ستون created at با شکست روبرو شد.",
                    e.getMessage()
            );
        }
    }
}