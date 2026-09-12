package com.alexistdev.geolicense.starter.service;

import com.alexistdev.geolicense.starter.properties.GeoLicenseProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * Persists the activation token so a restart can re-use the existing activation
 * instead of asking the server for a new one.
 */
public class LicenseStateStore {

    private static final Logger log = LoggerFactory.getLogger(LicenseStateStore.class);

    private final GeoLicenseProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LicenseStateStore(GeoLicenseProperties properties) {
        this.properties = properties;
    }

    public record CachedActivation(String machineId, String token) {
    }

    public CachedActivation load() {
        Path file = activationFile();
        try {
            if (!Files.exists(file)) {
                return null;
            }
            JsonNode root = objectMapper.readTree(Files.readString(file));
            String machineId = root.path("machineId").asText(null);
            String token = root.path("token").asText(null);
            if (machineId == null || machineId.isBlank() || token == null || token.isBlank()) {
                return null;
            }
            return new CachedActivation(machineId, token);
        } catch (Exception e) {
            log.warn("Unable to read cached activation from {}: {}", file, e.getMessage());
            return null;
        }
    }

    public void save(String machineId, String token) {
        Path file = activationFile();
        try {
            Files.createDirectories(file.getParent());
            String json = objectMapper.createObjectNode()
                    .put("machineId", machineId)
                    .put("token", token)
                    .toString();
            Files.writeString(file, json);
            restrictPermissions(file);
        } catch (Exception e) {
            log.warn("Unable to cache activation token in {} — the next start will activate again: {}",
                    file, e.getMessage());
        }
    }

    public void clear() {
        try {
            Files.deleteIfExists(activationFile());
        } catch (Exception e) {
            log.debug("Unable to delete cached activation: {}", e.getMessage());
        }
    }

    private Path activationFile() {
        String scope = properties.getLicenseKey() + "|" + properties.getProductSku();
        return Paths.get(properties.getStateDir(), "activation-" + shortHash(scope) + ".json");
    }

    private void restrictPermissions(Path file) {
        try {
            Set<PosixFilePermission> ownerOnly = PosixFilePermissions.fromString("rw-------");
            Files.setPosixFilePermissions(file, ownerOnly);
        } catch (Exception e) {
            log.debug("Unable to restrict permissions on {}: {}", file, e.getMessage());
        }
    }

    private String shortHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes())).substring(0, 16);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
