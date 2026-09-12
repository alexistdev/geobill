package com.alexistdev.geobill.models.entity;

import org.apache.commons.lang3.EnumUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TicketStatusTest {

    @Test
    void testEnumValues() {
        TicketStatus[] statuses = TicketStatus.values();
        Assertions.assertEquals(6, statuses.length);
        Assertions.assertEquals(TicketStatus.AWAITING_STAFF, statuses[0]);
        Assertions.assertEquals(TicketStatus.AWAITING_CLIENT, statuses[1]);
        Assertions.assertEquals(TicketStatus.IN_PROGRESS, statuses[2]);
        Assertions.assertEquals(TicketStatus.ON_HOLD, statuses[3]);
        Assertions.assertEquals(TicketStatus.CLOSED, statuses[4]);
        Assertions.assertEquals(TicketStatus.TRASH, statuses[5]);
    }

    @Test
    void testEnumValuesExist() {
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketStatus.class, "AWAITING_STAFF"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketStatus.class, "IN_PROGRESS"));
        Assertions.assertTrue(EnumUtils.isValidEnum(TicketStatus.class, "TRASH"));
    }

    @Test
    void testEnumValuesNotExist() {
        Assertions.assertFalse(EnumUtils.isValidEnum(TicketStatus.class, "OPEN"));
        Assertions.assertFalse(EnumUtils.isValidEnum(TicketStatus.class, "awaiting_staff"));
    }

    @Test
    void testEnumValueOf() {
        Assertions.assertEquals(TicketStatus.ON_HOLD, TicketStatus.valueOf("ON_HOLD"));
        Assertions.assertEquals(TicketStatus.CLOSED, TicketStatus.valueOf("CLOSED"));
    }

    @Test
    @DisplayName("getKey harus sama dengan key tab pada halaman admin ticket")
    void testGetKeyMatchesFrontendTabs() {
        Assertions.assertEquals("awaiting_staff", TicketStatus.AWAITING_STAFF.getKey());
        Assertions.assertEquals("awaiting_client", TicketStatus.AWAITING_CLIENT.getKey());
        Assertions.assertEquals("in_progress", TicketStatus.IN_PROGRESS.getKey());
        Assertions.assertEquals("on_hold", TicketStatus.ON_HOLD.getKey());
        Assertions.assertEquals("closed", TicketStatus.CLOSED.getKey());
        Assertions.assertEquals("trash", TicketStatus.TRASH.getKey());
    }

    @Test
    @DisplayName("Setiap key harus unik dan bisa dikembalikan menjadi enum")
    void testKeyIsReversible() {
        for (TicketStatus status : TicketStatus.values()) {
            Assertions.assertEquals(status, TicketStatus.valueOf(status.getKey().toUpperCase()));
        }
    }
}
