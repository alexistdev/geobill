package com.alexistdev.geolicense.starter.filter;

import com.alexistdev.geolicense.starter.properties.GeoLicenseProperties;
import com.alexistdev.geolicense.starter.service.LicenseHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class LicenseValidationFilter extends OncePerRequestFilter {

    private final GeoLicenseProperties properties;
    private final LicenseHolder licenseHolder;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LicenseValidationFilter(GeoLicenseProperties properties, LicenseHolder licenseHolder) {
        this.properties = properties;
        this.licenseHolder = licenseHolder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isExcluded(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (licenseHolder.isValid() || isWithinGracePeriod()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("status", false, "messages", new String[]{"License invalid or expired"})
        ));
    }

    private boolean isExcluded(String path) {
        return properties.getExcludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private boolean isWithinGracePeriod() {
        Instant lastValid = licenseHolder.getLastValidAt();
        if (lastValid == null) return false;
        long minutesSince = ChronoUnit.MINUTES.between(lastValid, Instant.now());
        return minutesSince <= properties.getGracePeriodMinutes();
    }
}
