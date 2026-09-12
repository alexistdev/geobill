package com.alexistdev.geobill.utils;

import jakarta.validation.Validator;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Validator yang membaca pesan dari bundle, sama seperti yang dipakai aplikasi.
 * Validator bawaan Jakarta hanya membaca ValidationMessages.properties, sehingga pesan
 * berbentuk {key} akan dikembalikan apa adanya dan test tidak menguji apa pun.
 */
public final class TestValidators {

    private TestValidators() {
    }

    public static Validator create() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        validator.afterPropertiesSet();
        return validator;
    }
}
