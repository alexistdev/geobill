package com.alexistdev.geolicense.starter.service;

import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.UUID;

public class MachineIdGenerator {

    private static final String MACHINE_ID_FILE = System.getProperty("user.home") + "/.geolicense-machine-id";

    public String generate() {
        try {
            String raw = getMacAddress() + "|" + getHostname();
            return sha256(raw);
        } catch (Exception e) {
            return getOrCreateFileBasedId();
        }
    }

    private String getMacAddress() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            byte[] mac = ni.getHardwareAddress();
            if (mac != null && mac.length > 0) {
                return HexFormat.of().formatHex(mac);
            }
        }
        throw new IllegalStateException("No MAC address found");
    }

    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }

    private String sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes());
        return HexFormat.of().formatHex(hash);
    }

    private String getOrCreateFileBasedId() {
        Path path = Paths.get(MACHINE_ID_FILE);
        try {
            if (Files.exists(path)) {
                return Files.readString(path).trim();
            }
            String id = UUID.randomUUID().toString().replace("-", "");
            Files.writeString(path, id);
            return id;
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }
}
