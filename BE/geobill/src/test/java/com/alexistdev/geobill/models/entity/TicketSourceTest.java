package com.alexistdev.geobill.models.entity;

import org.apache.commons.lang3.EnumUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TicketSourceTest {

    @Test
    void testEnumValues() {
        TicketSource[] sources = TicketSource.values();
        Assertions.assertEquals(3, sources.length);
        Assertions.assertEquals(TicketSource.WEB, sources[0]);
        Assertions.assertEquals(TicketSource.EMAIL, sources[1]);
        Assertions.assertEquals(TicketSource.API, sources[2]);
    }

    @Test
    void testEnumValuesExist() {
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketSource.class, "WEB"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketSource.class, "EMAIL"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketSource.class, "API"));
    }

    @Test
    void testEnumValuesNotExist() {
        Assertions.assertFalse(EnumUtils.isValidEnum(TicketSource.class, "MOBILE"));
        Assertions.assertFalse(EnumUtils.isValidEnum(TicketSource.class, "WHATSAPP"));
    }

    @Test
    void testEnumValueOf() {
        Assertions.assertEquals(TicketSource.WEB, TicketSource.valueOf("WEB"));
        Assertions.assertEquals(TicketSource.API, TicketSource.valueOf("API"));
    }
}
