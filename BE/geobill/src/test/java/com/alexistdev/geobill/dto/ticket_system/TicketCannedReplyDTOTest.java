package com.alexistdev.geobill.dto.ticket_system;

import org.junit.jupiter.api.*;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketCannedReplyDTOTest {

    private TicketCannedReplyDTO cannedReplyDTO;

    @BeforeEach
    void setUp() {
        cannedReplyDTO = new TicketCannedReplyDTO();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Set and Get Id")
    void testSetAndGetId() {
        String id = UUID.randomUUID().toString();
        cannedReplyDTO.setId(id);
        Assertions.assertEquals(id, cannedReplyDTO.getId());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set and Get")
    void testSetAndGet() {
        String departmentId = UUID.randomUUID().toString();

        cannedReplyDTO.setDepartmentId(departmentId);
        cannedReplyDTO.setDepartment("Technical Support");
        cannedReplyDTO.setTitle("Minta akses cPanel");
        cannedReplyDTO.setBody("Mohon kirimkan akses cPanel sementara");
        cannedReplyDTO.setActive(true);
        cannedReplyDTO.setSortOrder(2);

        Assertions.assertEquals(departmentId, cannedReplyDTO.getDepartmentId());
        Assertions.assertEquals("Technical Support", cannedReplyDTO.getDepartment());
        Assertions.assertEquals("Minta akses cPanel", cannedReplyDTO.getTitle());
        Assertions.assertEquals("Mohon kirimkan akses cPanel sementara", cannedReplyDTO.getBody());
        Assertions.assertTrue(cannedReplyDTO.isActive());
        Assertions.assertEquals(2, cannedReplyDTO.getSortOrder());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Global Template Has No Department")
    void testGlobalTemplate() {
        cannedReplyDTO.setTitle("Sapaan global");
        cannedReplyDTO.setDepartmentId(null);
        cannedReplyDTO.setDepartment(null);

        Assertions.assertNull(cannedReplyDTO.getDepartmentId(),
                "Template tanpa departemen berlaku untuk semua departemen");
        Assertions.assertNull(cannedReplyDTO.getDepartment());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Default Value")
    void testDefaultValue() {
        Assertions.assertNull(cannedReplyDTO.getId());
        Assertions.assertNull(cannedReplyDTO.getSortOrder());
        Assertions.assertFalse(cannedReplyDTO.isActive());
    }
}
