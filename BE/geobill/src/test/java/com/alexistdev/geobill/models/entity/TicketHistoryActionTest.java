package com.alexistdev.geobill.models.entity;

import org.apache.commons.lang3.EnumUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TicketHistoryActionTest {

    @Test
    void testEnumValues() {
        Assertions.assertEquals(11, TicketHistoryAction.values().length);
        Assertions.assertEquals(TicketHistoryAction.CREATED, TicketHistoryAction.values()[0]);
    }

    @Test
    @DisplayName("Aksi yang dipakai alur status tiket harus tersedia")
    void testEnumValuesExist() {
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketHistoryAction.class, "CREATED"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketHistoryAction.class, "STATUS_CHANGED"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketHistoryAction.class, "PRIORITY_CHANGED"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketHistoryAction.class, "ASSIGNED"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketHistoryAction.class, "UNASSIGNED"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketHistoryAction.class, "DEPARTMENT_CHANGED"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketHistoryAction.class, "REPLIED"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketHistoryAction.class, "TRASHED"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketHistoryAction.class, "RESTORED"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketHistoryAction.class, "CLOSED"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketHistoryAction.class, "REOPENED"));
    }

    @Test
    void testEnumValuesNotExist() {
        Assertions.assertFalse(EnumUtils.isValidEnum(TicketHistoryAction.class, "DELETED"));
        Assertions.assertFalse(EnumUtils.isValidEnum(TicketHistoryAction.class, "MERGED"));
    }

    @Test
    void testEnumValueOf() {
        Assertions.assertEquals(TicketHistoryAction.RESTORED, TicketHistoryAction.valueOf("RESTORED"));
        Assertions.assertEquals(TicketHistoryAction.REPLIED, TicketHistoryAction.valueOf("REPLIED"));
    }
}
