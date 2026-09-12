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

public class LicenseActivationService {

    private static final Logger log = LoggerFactory.getLogger(LicenseActivationService.class);

    private final GeoLicenseProperties properties;
    private final LicenseHolder licenseHolder;
    private final MachineIdGenerator machineIdGenerator;
    private final LicenseStateStore stateStore;
    private final LicenseVerificationService verificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public LicenseActivationService(GeoLicenseProperties properties,
                                    LicenseHolder licenseHolder,
                                    MachineIdGenerator machineIdGenerator,
                                    LicenseStateStore stateStore,
                                    LicenseVerificationService verificationService) {
        this.properties = properties;
        this.licenseHolder = licenseHolder;
        this.machineIdGenerator = machineIdGenerator;
        this.stateStore = stateStore;
        this.verificationService = verificationService;
    }

    /**
     * Re-uses the activation stored on this machine when it is still accepted by the server, and
     * only activates when there is nothing usable. Activation is what consumes a seat, so a plain
     * restart or redeploy must not reach {@link #activate()}.
     */
    public void ensureActivated() {
        requireProductSku();

        String machineId = machineIdGenerator.generate();
        LicenseStateStore.CachedActivation cached = stateStore.load();

        if (cached == null) {
            log.info("No stored activation found for this machine — activating");
            activate();
            return;
        }

        if (!cached.machineId().equals(machineId)) {
            log.info("Stored activation belongs to a different machine id — activating");
            stateStore.clear();
            activate();
            return;
        }

        licenseHolder.setToken(cached.token());
        VerificationResult result = verificationService.verify();

        switch (result) {
            case VALID -> log.info("Re-using the existing activation for key: {} — no additional seat used",
                    maskKey(properties.getLicenseKey()));
            case UNREACHABLE -> {
                licenseHolder.setValid(true);
                log.warn("License server unreachable at startup — starting on the stored activation, "
                        + "traffic is allowed for the next {} minutes", properties.getGracePeriodMinutes());
            }
            case INVALID -> {
                log.info("Stored activation was rejected — activating again");
                stateStore.clear();
                activate();
            }
        }
    }

    public void activate() {
        requireProductSku();

        String machineId = machineIdGenerator.generate();
        String osInfo = System.getProperty("os.name") + " " + System.getProperty("os.version");

        String body = objectMapper.createObjectNode()
                .put("licenseKey", properties.getLicenseKey())
                .put("machineId", machineId)
                .put("productSku", properties.getProductSku())
                .put("osInfo", osInfo)
                .toString();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getServerUrl() + "/api/v1/licenses/activate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IllegalStateException("License activation failed — HTTP " + response.statusCode()
                        + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            boolean status = root.path("status").asBoolean(false);

            if (!status) {
                String message = root.path("messages").path(0).asText("activation rejected");
                throw new IllegalStateException("License activation rejected: " + message);
            }

            String token = root.path("payload").path("token").asText(null);
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("License activation response missing token");
            }

            licenseHolder.setToken(token);
            licenseHolder.setValid(true);
            stateStore.save(machineId, token);
            log.info("License activated successfully for key: {}", maskKey(properties.getLicenseKey()));

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to reach license server at " + properties.getServerUrl(), e);
        }
    }

    private void requireProductSku() {
        String productSku = properties.getProductSku();
        if (productSku == null || productSku.isBlank()) {
            throw new IllegalStateException("geolicense.product-sku is required to activate a license");
        }
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 8) return "***";
        return key.substring(0, 8) + "****";
    }
}
