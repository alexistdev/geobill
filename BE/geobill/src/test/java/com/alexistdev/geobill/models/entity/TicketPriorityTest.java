package com.alexistdev.geobill.models.entity;

import org.apache.commons.lang3.EnumUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TicketPriorityTest {

    @Test
    void testEnumValues() {
        TicketPriority[] priorities = TicketPriority.values();
        Assertions.assertEquals(4, priorities.length);
        Assertions.assertEquals(TicketPriority.LOW, priorities[0]);
        Assertions.assertEquals(TicketPriority.MEDIUM, priorities[1]);
        Assertions.assertEquals(TicketPriority.HIGH, priorities[2]);
        Assertions.assertEquals(TicketPriority.URGENT, priorities[3]);
    }

    @Test
    void testEnumValuesExist() {
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketPriority.class, "LOW"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketPriority.class, "MEDIUM"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketPriority.class, "HIGH"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketPriority.class, "URGENT"));
    }

    @Test
    void testEnumValuesNotExist() {
        Assertions.assertFalse(EnumUtils.isValidEnum(TicketPriority.class, "CRITICAL"));
        Assertions.assertFalse(EnumUtils.isValidEnum(TicketPriority.class, "Any"));
    }

    @Test
    void testEnumOrdinalMenaik() {
        Assertions.assertTrue(TicketPriority.LOW.ordinal() < TicketPriority.MEDIUM.ordinal());
        Assertions.assertTrue(TicketPriority.MEDIUM.ordinal() < TicketPriority.HIGH.ordinal());
        Assertions.assertTrue(TicketPriority.HIGH.ordinal() < TicketPriority.URGENT.ordinal());
    }

    @Test
    void testEnumValueOf() {
        Assertions.assertEquals(TicketPriority.HIGH, TicketPriority.valueOf("HIGH"));
        Assertions.assertEquals(TicketPriority.LOW, TicketPriority.valueOf("LOW"));
    }
}
