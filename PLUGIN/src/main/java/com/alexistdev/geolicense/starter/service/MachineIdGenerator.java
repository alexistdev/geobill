package com.alexistdev.geolicense.starter.service;

import com.alexistdev.geolicense.starter.properties.GeoLicenseProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Produces a machine id that stays the same for the lifetime of the installation.
 *
 * <p>The id is resolved once and then persisted. Hardware detection is only a seed for the very
 * first run, because virtual and randomised interfaces (awdl0, llw0, utun*, docker0, bridge0,
 * private Wi-Fi addresses) change on their own and would otherwise make every restart look like a
 * brand-new machine to the license server — which consumes a new seat each time.
 */
public class MachineIdGenerator {

    private static final Logger log = LoggerFactory.getLogger(MachineIdGenerator.class);

    /** File written by releases up to 1.0.2; still honoured so existing seats are kept. */
    private static final Path LEGACY_MACHINE_ID_FILE =
            Paths.get(System.getProperty("user.home"), ".geolicense-machine-id");

    private static final String MACHINE_ID_FILE_NAME = "machine-id";

    /** Interfaces whose hardware address is virtual, randomised or recreated per run. */
    private static final List<String> UNSTABLE_INTERFACE_PREFIXES = List.of(
            "awdl", "llw", "utun", "tun", "tap", "bridge", "vmnet", "vnic", "docker",
            "veth", "vboxnet", "anpi", "ap", "lo", "ham", "zt", "wg", "gpd", "ipsec");

    private final GeoLicenseProperties properties;
    private volatile String cachedId;

    public MachineIdGenerator(GeoLicenseProperties properties) {
        this.properties = properties;
    }

    public String generate() {
        String id = cachedId;
        if (id != null) {
            return id;
        }
        synchronized (this) {
            if (cachedId == null) {
                cachedId = resolve();
            }
            return cachedId;
        }
    }

    private String resolve() {
        String configured = properties.getMachineId();
        if (configured != null && !configured.isBlank()) {
            log.info("Using machine id from geolicense.machine-id");
            return configured.trim();
        }

        Path stateFile = machineIdFile();
        String persisted = readIfPresent(stateFile);
        if (persisted != null) {
            return persisted;
        }

        String legacy = readIfPresent(LEGACY_MACHINE_ID_FILE);
        if (legacy != null) {
            write(stateFile, legacy);
            return legacy;
        }

        String derived = deriveFromStableHardware();
        if (derived == null) {
            derived = UUID.randomUUID().toString().replace("-", "");
            log.warn("No stable hardware address found — generated a persistent random machine id");
        }
        write(stateFile, derived);
        log.info("Registered machine id for this installation (stored in {})", stateFile);
        return derived;
    }

    private Path machineIdFile() {
        return Paths.get(properties.getStateDir(), MACHINE_ID_FILE_NAME);
    }

    /**
     * Hashes every physical, globally-administered MAC address found, sorted so the enumeration
     * order of the interfaces cannot change the result.
     */
    private String deriveFromStableHardware() {
        List<String> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!isStable(ni)) {
                    continue;
                }
                byte[] mac = ni.getHardwareAddress();
                if (mac != null && mac.length > 0 && isGloballyAdministered(mac)) {
                    addresses.add(HexFormat.of().formatHex(mac));
                }
            }
        } catch (Exception e) {
            log.debug("Unable to enumerate network interfaces: {}", e.getMessage());
            return null;
        }

        if (addresses.isEmpty()) {
            return null;
        }

        addresses.sort(String::compareTo);
        return sha256(String.join("|", addresses));
    }

    private boolean isStable(NetworkInterface ni) {
        try {
            if (ni.isLoopback() || ni.isVirtual() || ni.isPointToPoint()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        String name = ni.getName().toLowerCase(Locale.ROOT);
        return UNSTABLE_INTERFACE_PREFIXES.stream().noneMatch(name::startsWith);
    }

    /** The second-least-significant bit of the first octet marks locally administered (randomised) MACs. */
    private boolean isGloballyAdministered(byte[] mac) {
        return (mac[0] & 0x02) == 0;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes()));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String readIfPresent(Path path) {
        try {
            if (Files.exists(path)) {
                String value = Files.readString(path).trim();
                if (!value.isBlank()) {
                    return value;
                }
            }
        } catch (Exception e) {
            log.warn("Unable to read machine id from {}: {}", path, e.getMessage());
        }
        return null;
    }

    private void write(Path path, String id) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, id);
        } catch (Exception e) {
            log.warn("Unable to persist machine id to {} — a new seat may be consumed on the next start: {}",
                    path, e.getMessage());
        }
    }
}
