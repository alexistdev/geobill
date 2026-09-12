package com.alexistdev.geobill.request;

import com.alexistdev.geobill.request.ticket_system.TicketRequest;
import com.alexistdev.geobill.utils.TestValidators;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Membuktikan pesan Bean Validation ikut locale request, bukan teks tetap di dalam anotasi.
 */
class ValidationMessageI18nTest {

    private final Validator validator = TestValidators.create();

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    private String messageFor(Locale locale) {
        LocaleContextHolder.setLocale(locale);

        TicketRequest request = new TicketRequest();
        request.setDepartmentId("11111111-1111-4111-8111-111111111111");
        request.setMessage("halo");

        Set<ConstraintViolation<TicketRequest>> violations = validator.validate(request);
        return violations.stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("subject"))
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Pesan validasi berbahasa Inggris saat locale en")
    void validationMessage_inEnglish() {
        assertEquals("subject is required", messageFor(Locale.ENGLISH));
    }

    @Test
    @DisplayName("Pesan validasi berbahasa Indonesia saat locale id")
    void validationMessage_inIndonesian() {
        assertEquals("subject wajib diisi", messageFor(Locale.forLanguageTag("id")));
    }
}
