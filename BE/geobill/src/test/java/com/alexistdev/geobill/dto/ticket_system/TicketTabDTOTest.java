package com.alexistdev.geobill.dto.ticket_system;

import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketTabDTOTest {

    private TicketTabDTO tabDTO;

    @BeforeEach
    void setUp() {
        tabDTO = new TicketTabDTO();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Set and Get")
    void testSetAndGet() {
        tabDTO.setKey("awaiting_staff");
        tabDTO.setLabel("Awaiting Staff");
        tabDTO.setCount(4);

        Assertions.assertEquals("awaiting_staff", tabDTO.getKey());
        Assertions.assertEquals("Awaiting Staff", tabDTO.getLabel());
        Assertions.assertEquals(4, tabDTO.getCount());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Key Matches The Angular Tab Key")
    void testKeyMatchesFrontendTab() {
        tabDTO.setKey("in_progress");

        Assertions.assertEquals("in_progress", tabDTO.getKey());
        Assertions.assertEquals(tabDTO.getKey(), tabDTO.getKey().toLowerCase(),
                "Key tab selalu huruf kecil, sama dengan komponen Angular");
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Empty Tab Counts Zero")
    void testEmptyTab() {
        tabDTO.setKey("closed");
        tabDTO.setLabel("Closed");

        Assertions.assertEquals(0, tabDTO.getCount(), "Tab tanpa tiket bernilai nol, bukan null");
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Default Value")
    void testDefaultValue() {
        Assertions.assertNull(tabDTO.getKey());
        Assertions.assertNull(tabDTO.getLabel());
        Assertions.assertEquals(0, tabDTO.getCount());
    }
}
