package com.alexistdev.geolicense.starter.service;

import com.alexistdev.geolicense.starter.properties.GeoLicenseProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LicenseVerificationService {

    private static final Logger log = LoggerFactory.getLogger(LicenseVerificationService.class);

    private final GeoLicenseProperties properties;
    private final LicenseHolder licenseHolder;
    private final MachineIdGenerator machineIdGenerator;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public LicenseVerificationService(GeoLicenseProperties properties,
                                      LicenseHolder licenseHolder,
                                      MachineIdGenerator machineIdGenerator) {
        this.properties = properties;
        this.licenseHolder = licenseHolder;
        this.machineIdGenerator = machineIdGenerator;
    }

    public VerificationResult verify() {
        String token = licenseHolder.getToken();
        if (token == null) {
            log.warn("Verification skipped — no license token present");
            licenseHolder.setValid(false);
            return VerificationResult.INVALID;
        }

        String machineId = machineIdGenerator.generate();
        String body = objectMapper.createObjectNode()
                .put("token", token)
                .put("machineId", machineId)
                .put("productSku", properties.getProductSku())
                .toString();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getServerUrl() + "/api/v1/licenses/verify"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            boolean status = root.path("status").asBoolean(false);

            if (response.statusCode() == 200 && status) {
                licenseHolder.setValid(true);
                log.debug("License verified successfully");
                return VerificationResult.VALID;
            }

            licenseHolder.setValid(false);
            String message = root.path("messages").path(0).asText("unknown reason");
            log.warn("License verification failed: {}", message);
            return VerificationResult.INVALID;

        } catch (Exception e) {
            log.warn("License server unreachable during verification: {}", e.getMessage());
            return VerificationResult.UNREACHABLE;
        }
    }
}
