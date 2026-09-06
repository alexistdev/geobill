package com.alexistdev.geolicense.starter.service;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
public class LicenseHolder {

    @Setter
    private volatile String token;
    private volatile boolean valid = false;
    private volatile Instant lastValidAt;

    public void setValid(boolean valid) {
        this.valid = valid;
        if (valid) this.lastValidAt = Instant.now();
    }

}
