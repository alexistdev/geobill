package com.alexistdev.geolicense.starter.scheduler;

import com.alexistdev.geolicense.starter.service.LicenseVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class LicenseVerificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(LicenseVerificationScheduler.class);

    private final LicenseVerificationService verificationService;

    public LicenseVerificationScheduler(LicenseVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @Scheduled(fixedDelayString = "${geolicense.verify-interval-ms:3600000}")
    public void run() {
        log.debug("Running scheduled license verification");
        verificationService.verify();
    }
}
