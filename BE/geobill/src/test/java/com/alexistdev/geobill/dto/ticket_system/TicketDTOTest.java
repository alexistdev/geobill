package com.alexistdev.geobill.dto.ticket_system;

import org.junit.jupiter.api.*;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketDTOTest {

    private TicketDTO ticketDTO;

    @BeforeEach
    void setUp() {
        ticketDTO = new TicketDTO();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Set and Get Id")
    void testSetAndGetId() {
        String id = UUID.randomUUID().toString();
        ticketDTO.setId(id);
        Assertions.assertEquals(id, ticketDTO.getId());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set and Get")
    void testSetAndGet() {
        String departmentId = UUID.randomUUID().toString();
        String assignedToId = UUID.randomUUID().toString();

        ticketDTO.setNumber("TKT-202609-000001");
        ticketDTO.setSummary("Website down");
        ticketDTO.setPriority("HIGH");
        ticketDTO.setDepartment("Technical Support");
        ticketDTO.setDepartmentId(departmentId);
        ticketDTO.setAssignedTo("Staff One");
        ticketDTO.setAssignedToId(assignedToId);
        ticketDTO.setRequester("Client One");
        ticketDTO.setLastReply("07-09-2026 10:30");
        ticketDTO.setStatus("in_progress");
        ticketDTO.setReplyCount(3);

        Assertions.assertEquals("TKT-202609-000001", ticketDTO.getNumber());
        Assertions.assertEquals("Website down", ticketDTO.getSummary());
        Assertions.assertEquals("HIGH", ticketDTO.getPriority());
        Assertions.assertEquals("Technical Support", ticketDTO.getDepartment());
        Assertions.assertEquals(departmentId, ticketDTO.getDepartmentId());
        Assertions.assertEquals("Staff One", ticketDTO.getAssignedTo());
        Assertions.assertEquals(assignedToId, ticketDTO.getAssignedToId());
        Assertions.assertEquals("Client One", ticketDTO.getRequester());
        Assertions.assertEquals("07-09-2026 10:30", ticketDTO.getLastReply());
        Assertions.assertEquals("in_progress", ticketDTO.getStatus());
        Assertions.assertEquals(3, ticketDTO.getReplyCount());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Default Value")
    void testDefaultValue() {
        Assertions.assertNull(ticketDTO.getId());
        Assertions.assertNull(ticketDTO.getStatus());
        Assertions.assertNull(ticketDTO.getAssignedToId());
        Assertions.assertEquals(0, ticketDTO.getReplyCount());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Unassigned Ticket Keeps Label Without Id")
    void testUnassignedTicket() {
        ticketDTO.setAssignedTo("Unassigned");
        ticketDTO.setAssignedToId(null);

        Assertions.assertEquals("Unassigned", ticketDTO.getAssignedTo());
        Assertions.assertNull(ticketDTO.getAssignedToId());
    }
}
