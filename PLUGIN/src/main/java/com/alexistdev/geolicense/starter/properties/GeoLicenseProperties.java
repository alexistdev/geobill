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

}
