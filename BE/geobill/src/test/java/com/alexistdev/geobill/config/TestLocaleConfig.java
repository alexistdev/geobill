package com.alexistdev.geobill.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
public class TestLocaleConfig {

    @Value("${app.locale:en_US}")
    private String defaultLocale;

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        String[] localeParts = defaultLocale.split("_");
        Locale locale = new Locale(localeParts[0], localeParts[1]);
        resolver.setDefaultLocale(locale);
        return resolver;
    }

}
