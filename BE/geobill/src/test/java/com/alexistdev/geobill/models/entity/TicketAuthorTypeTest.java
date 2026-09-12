package com.alexistdev.geobill.models.entity;

import org.apache.commons.lang3.EnumUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TicketAuthorTypeTest {

    @Test
    void testEnumValues() {
        TicketAuthorType[] types = TicketAuthorType.values();
        Assertions.assertEquals(3, types.length);
        Assertions.assertEquals(TicketAuthorType.CLIENT, types[0]);
        Assertions.assertEquals(TicketAuthorType.STAFF, types[1]);
        Assertions.assertEquals(TicketAuthorType.SYSTEM, types[2]);
    }

    @Test
    void testEnumValuesExist() {
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketAuthorType.class, "CLIENT"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketAuthorType.class, "STAFF"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketAuthorType.class, "SYSTEM"));
    }

    @Test
    void testEnumValuesNotExist() {
        Assertions.assertFalse(EnumUtils.isValidEnum(TicketAuthorType.class, "ADMIN"));
        Assertions.assertFalse(EnumUtils.isValidEnum(TicketAuthorType.class, "USER"));
    }

    @Test
    void testEnumValueOf() {
        Assertions.assertEquals(TicketAuthorType.SYSTEM, TicketAuthorType.valueOf("SYSTEM"));
        Assertions.assertEquals(TicketAuthorType.CLIENT, TicketAuthorType.valueOf("CLIENT"));
    }
}
