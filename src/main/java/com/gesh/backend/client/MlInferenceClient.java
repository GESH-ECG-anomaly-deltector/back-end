package com.gesh.backend.client;

import com.gesh.backend.model.Diagnosis;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MlInferenceClient {

    private final RestClient model2Client;
    private final RestClient model3Client;

    public MlInferenceClient(
            @Value("${ml.service.model2-url}") String model2BaseUrl,
            @Value("${ml.service.model3-url}") String model3BaseUrl
    ) {
        this.model2Client = RestClient.builder().baseUrl(model2BaseUrl).build();
        this.model3Client = RestClient.builder().baseUrl(model3BaseUrl).build();
    }


    public List<Diagnosis> classify(float[][] signal) {
        List<Diagnosis> combined = new ArrayList<>();
        combined.addAll(callModel(model2Client, signal));
        combined.addAll(callModel(model3Client, signal));
        return combined;
    }

    private List<Diagnosis> callModel(RestClient client, float[][] signal) {
        Diagnosis[] result = client.post()
                .uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("signal", signal))
                .retrieve()
                .body(Diagnosis[].class);

        return result != null ? List.of(result) : List.of();
    }
}
