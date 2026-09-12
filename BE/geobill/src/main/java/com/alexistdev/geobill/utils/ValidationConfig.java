package com.alexistdev.geobill.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Menjadikan bundle pesan sebagai sumber pesan Bean Validation, sehingga pesan berbentuk
 * {@code {key}} pada anotasi seperti {@code @NotBlank} dan {@code @Size} diterjemahkan
 * mengikuti locale request. Tanpa bean ini, Hibernate Validator hanya membaca
 * ValidationMessages.properties dan key yang tidak ditemukan dikirim apa adanya ke klien.
 */
@Configuration
public class ValidationConfig {

    @Bean
    public LocalValidatorFactoryBean defaultValidator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
