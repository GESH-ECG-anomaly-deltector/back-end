package com.gesh.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.gesh.backend.model.Admin;
import com.gesh.backend.model.Doctor;
import com.gesh.backend.model.EcgRecord;
import com.gesh.backend.model.Patient;
import com.gesh.backend.repository.AdminRepository;
import com.gesh.backend.repository.DoctorRepository;
import com.gesh.backend.repository.EcgRecordRepository;
import com.gesh.backend.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;
    private final EcgRecordRepository ecgRecordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataSeeder(PatientRepository patientRepository, DoctorRepository doctorRepository,
                       AdminRepository adminRepository, EcgRecordRepository ecgRecordRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.adminRepository = adminRepository;
        this.ecgRecordRepository = ecgRecordRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seedMissing(patientRepository, readJsonList("data/patientsData.json", Patient.class), Patient::getId);
        seedMissing(doctorRepository, readJsonList("data/doctorsData.json", Doctor.class), Doctor::getId);
        seedMissing(adminRepository, readJsonList("data/adminData.json", Admin.class), Admin::getId);
        seedMissing(ecgRecordRepository, readJsonList("data/recordsData.json", EcgRecord.class), EcgRecord::getRecId);
    }

    private <T, ID> void seedMissing(JpaRepository<T, ID> repository, List<T> items, Function<T, ID> idExtractor) {
        for (T item : items) {
            if (!repository.existsById(idExtractor.apply(item))) {
                repository.save(item);
            }
        }
    }

    private <T> List<T> readJsonList(String classpathLocation, Class<T> elementType) throws Exception {
        try (InputStream stream = new ClassPathResource(classpathLocation).getInputStream()) {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, elementType);
            return objectMapper.readValue(stream, listType);
        }
    }
}
