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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;
    private final EcgRecordRepository ecgRecordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DemoDataSeeder(PatientRepository patientRepository, DoctorRepository doctorRepository,
                           AdminRepository adminRepository, EcgRecordRepository ecgRecordRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.adminRepository = adminRepository;
        this.ecgRecordRepository = ecgRecordRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (doctorRepository.count() == 0) {
            doctorRepository.saveAll(readJsonList("data/doctorsData.json", Doctor.class));
        }
        if (patientRepository.count() == 0) {
            patientRepository.saveAll(readJsonList("data/patientsData.json", Patient.class));
        }
        if (adminRepository.count() == 0) {
            adminRepository.saveAll(readJsonList("data/adminData.json", Admin.class));
        }
        if (ecgRecordRepository.count() == 0) {
            ecgRecordRepository.saveAll(readJsonList("data/recordsData.json", EcgRecord.class));
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
