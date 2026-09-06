package com.alexistdev.geolicense.starter.autoconfigure;

import com.alexistdev.geolicense.starter.filter.LicenseValidationFilter;
import com.alexistdev.geolicense.starter.properties.GeoLicenseProperties;
import com.alexistdev.geolicense.starter.scheduler.LicenseVerificationScheduler;
import com.alexistdev.geolicense.starter.service.LicenseActivationService;
import com.alexistdev.geolicense.starter.service.LicenseHolder;
import com.alexistdev.geolicense.starter.service.LicenseVerificationService;
import com.alexistdev.geolicense.starter.service.MachineIdGenerator;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@EnableConfigurationProperties(GeoLicenseProperties.class)
@ConditionalOnProperty(prefix = "geolicense", name = "license-key")
@EnableScheduling
public class GeoLicenseAutoConfiguration {

    @Bean
    public MachineIdGenerator machineIdGenerator() {
        return new MachineIdGenerator();
    }

    @Bean
    public LicenseHolder licenseHolder() {
        return new LicenseHolder();
    }

    @Bean
    public LicenseActivationService licenseActivationService(GeoLicenseProperties properties,
                                                             LicenseHolder licenseHolder,
                                                             MachineIdGenerator machineIdGenerator) {
        return new LicenseActivationService(properties, licenseHolder, machineIdGenerator);
    }

    @Bean
    public LicenseVerificationService licenseVerificationService(GeoLicenseProperties properties,
                                                                 LicenseHolder licenseHolder,
                                                                 MachineIdGenerator machineIdGenerator) {
        return new LicenseVerificationService(properties, licenseHolder, machineIdGenerator);
    }

    @Bean
    @ConditionalOnWebApplication
    public LicenseValidationFilter licenseValidationFilter(GeoLicenseProperties properties,
                                                           LicenseHolder licenseHolder) {
        return new LicenseValidationFilter(properties, licenseHolder);
    }

    @Bean
    public LicenseVerificationScheduler licenseVerificationScheduler(LicenseVerificationService verificationService) {
        return new LicenseVerificationScheduler(verificationService);
    }

    // Runs at startup — fails app context if license activation fails (hard fail)
    @Bean
    public ApplicationRunner licenseActivationRunner(LicenseActivationService activationService) {
        return args -> activationService.activate();
    }
}
