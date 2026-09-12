package com.alexistdev.geobill.dto.ticket_system;

import org.junit.jupiter.api.*;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketDepartmentDTOTest {

    private TicketDepartmentDTO departmentDTO;

    @BeforeEach
    void setUp() {
        departmentDTO = new TicketDepartmentDTO();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Set and Get Id")
    void testSetAndGetId() {
        String id = UUID.randomUUID().toString();
        departmentDTO.setId(id);
        Assertions.assertEquals(id, departmentDTO.getId());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set and Get")
    void testSetAndGet() {
        departmentDTO.setName("Technical Support");
        departmentDTO.setCode("TECH");
        departmentDTO.setEmail("support@geobill.test");
        departmentDTO.setDescription("Kendala teknis layanan hosting");
        departmentDTO.setSortOrder(1);
        departmentDTO.setActive(true);
        departmentDTO.setAutoCloseDays(7);

        Assertions.assertEquals("Technical Support", departmentDTO.getName());
        Assertions.assertEquals("TECH", departmentDTO.getCode());
        Assertions.assertEquals("support@geobill.test", departmentDTO.getEmail());
        Assertions.assertEquals("Kendala teknis layanan hosting", departmentDTO.getDescription());
        Assertions.assertEquals(1, departmentDTO.getSortOrder());
        Assertions.assertTrue(departmentDTO.isActive());
        Assertions.assertEquals(7, departmentDTO.getAutoCloseDays());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Inactive Department")
    void testInactiveDepartment() {
        departmentDTO.setActive(false);
        departmentDTO.setAutoCloseDays(null);

        Assertions.assertFalse(departmentDTO.isActive());
        Assertions.assertNull(departmentDTO.getAutoCloseDays(),
                "Null berarti tidak pernah menutup tiket otomatis");
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Default Value")
    void testDefaultValue() {
        Assertions.assertNull(departmentDTO.getId());
        Assertions.assertNull(departmentDTO.getSortOrder());
        Assertions.assertFalse(departmentDTO.isActive());
    }
}
