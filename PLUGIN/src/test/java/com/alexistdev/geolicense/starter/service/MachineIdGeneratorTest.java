package com.alexistdev.geolicense.starter.service;

import com.alexistdev.geolicense.starter.properties.GeoLicenseProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineIdGeneratorTest {

    private GeoLicenseProperties properties(Path stateDir) {
        GeoLicenseProperties properties = new GeoLicenseProperties();
        properties.setStateDir(stateDir.toString());
        return properties;
    }

    @Test
    void generate_isStableAcrossRestarts(@TempDir Path stateDir) {
        String first = new MachineIdGenerator(properties(stateDir)).generate();
        String second = new MachineIdGenerator(properties(stateDir)).generate();

        assertEquals(first, second);
        assertTrue(Files.exists(stateDir.resolve("machine-id")));
    }

    @Test
    void generate_prefersConfiguredMachineId(@TempDir Path stateDir) {
        GeoLicenseProperties properties = properties(stateDir);
        properties.setMachineId("container-node-1");

        assertEquals("container-node-1", new MachineIdGenerator(properties).generate());
    }

    @Test
    void generate_reusesPersistedIdEvenWhenHardwareChanges(@TempDir Path stateDir) throws Exception {
        Files.createDirectories(stateDir);
        Files.writeString(stateDir.resolve("machine-id"), "previously-registered-id");

        assertEquals("previously-registered-id", new MachineIdGenerator(properties(stateDir)).generate());
    }
}
