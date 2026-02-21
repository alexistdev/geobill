package com.alexistdev.geobill.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessagesUtils {

    private final MessageSource messageSource;

    public MessagesUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    public String getMessage(String key, String param) {
        return String.format(messageSource.getMessage(key, null,
                LocaleContextHolder.getLocale()), param);
    }
}
