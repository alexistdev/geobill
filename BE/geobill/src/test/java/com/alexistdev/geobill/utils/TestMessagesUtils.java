package com.alexistdev.geobill.utils;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * MessagesUtils asli yang membaca bundle pesan dari classpath, bukan mock.
 * Dipakai di test supaya pesan yang diuji benar-benar berasal dari file properties,
 * sehingga key yang salah ketik atau hilang langsung ketahuan.
 */
public final class TestMessagesUtils {

    private TestMessagesUtils() {
    }

    public static MessagesUtils create() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return new MessagesUtils(messageSource);
    }
}
