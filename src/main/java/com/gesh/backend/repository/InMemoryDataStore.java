package com.gesh.backend.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
//importing all the models:
import com.gesh.backend.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InMemoryDataStore {

    private final List<Patient> patients = new CopyOnWriteArrayList<>();
    private final List<Doctor> doctors = new CopyOnWriteArrayList<>();
    private final List<EcgRecord> records = new CopyOnWriteArrayList<>();
    private final List<AssignmentRequest> assignmentRequests = new CopyOnWriteArrayList<>();
    private final List<Admin> admins = new CopyOnWriteArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadSeedData() throws Exception {
        patients.addAll(readJsonList("data/patientsData.json", Patient.class));
        doctors.addAll(readJsonList("data/doctorsData.json", Doctor.class));
        records.addAll(readJsonList("data/recordsData.json", EcgRecord.class));
        admins.addAll(readJsonList("data/adminData.json", Admin.class));
    }

    private <T> List<T> readJsonList(String classpathLocation, Class<T> elementType) throws Exception {
        try (InputStream stream = new ClassPathResource(classpathLocation).getInputStream()) {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, elementType);
            return objectMapper.readValue(stream, listType);
        }
    }

    public List<Patient> getPatients() { return patients; }
    public List<Doctor> getDoctors() { return doctors; }
    public List<EcgRecord> getRecords() { return records; }
    public List<AssignmentRequest> getAssignmentRequests() { return assignmentRequests; }
    public List<Admin> getAdmins() { return admins; }
}
