package com.alexistdev.geobill.dto.ticket_system;

import org.junit.jupiter.api.*;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketStaffDTOTest {

    private TicketStaffDTO staffDTO;

    @BeforeEach
    void setUp() {
        staffDTO = new TicketStaffDTO();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Set and Get Id")
    void testSetAndGetId() {
        String id = UUID.randomUUID().toString();
        staffDTO.setId(id);
        Assertions.assertEquals(id, staffDTO.getId());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set and Get")
    void testSetAndGet() {
        staffDTO.setFullName("Staff One");
        staffDTO.setEmail("staff1@geobill.test");
        staffDTO.setRole("STAFF");
        staffDTO.setSupervisor(true);

        Assertions.assertEquals("Staff One", staffDTO.getFullName());
        Assertions.assertEquals("staff1@geobill.test", staffDTO.getEmail());
        Assertions.assertEquals("STAFF", staffDTO.getRole());
        Assertions.assertTrue(staffDTO.isSupervisor());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Admin Can Also Handle Ticket")
    void testAdminStaff() {
        staffDTO.setRole("ADMIN");
        staffDTO.setSupervisor(false);

        Assertions.assertEquals("ADMIN", staffDTO.getRole());
        Assertions.assertFalse(staffDTO.isSupervisor());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Default Value")
    void testDefaultValue() {
        Assertions.assertNull(staffDTO.getId());
        Assertions.assertNull(staffDTO.getRole());
        Assertions.assertFalse(staffDTO.isSupervisor());
    }
}
