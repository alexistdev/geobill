package com.alexistdev.geolicense.starter.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "geolicense")
public class GeoLicenseProperties {

    private String serverUrl;
    private String licenseKey;
    private String productSku;
    private long verifyIntervalMs = 3_600_000L;
    private int gracePeriodMinutes = 30;
    private List<String> excludePaths = List.of("/actuator/**", "/health/**");

    /**
     * Explicit machine id. When set, it overrides hardware detection entirely.
     * Use this on containers/CI where network interfaces are recreated on every run,
     * otherwise every run looks like a new machine and burns a seat.
     */
    private String machineId;

    /**
     * Directory where the machine id and the cached activation token are persisted,
     * so a restart re-uses the existing activation instead of activating again.
     */
    private String stateDir = System.getProperty("user.home") + "/.geolicense";

}
