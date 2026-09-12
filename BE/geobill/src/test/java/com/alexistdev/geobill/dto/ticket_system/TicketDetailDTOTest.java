package com.alexistdev.geobill.dto.ticket_system;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketDetailDTOTest {

    private TicketDetailDTO detailDTO;

    @BeforeEach
    void setUp() {
        detailDTO = new TicketDetailDTO();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Set and Get Id")
    void testSetAndGetId() {
        String id = UUID.randomUUID().toString();
        detailDTO.setId(id);
        Assertions.assertEquals(id, detailDTO.getId());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set and Get")
    void testSetAndGet() {
        String departmentId = UUID.randomUUID().toString();
        String requesterId = UUID.randomUUID().toString();
        String hostingId = UUID.randomUUID().toString();
        String invoiceId = UUID.randomUUID().toString();

        detailDTO.setNumber("TKT-202609-000001");
        detailDTO.setSubject("Website down");
        detailDTO.setPriority("URGENT");
        detailDTO.setStatus("awaiting_staff");
        detailDTO.setSource("WEB");
        detailDTO.setDepartment("Technical Support");
        detailDTO.setDepartmentId(departmentId);
        detailDTO.setRequester("Client One");
        detailDTO.setRequesterId(requesterId);
        detailDTO.setAssignedTo("Unassigned");
        detailDTO.setAssignedToId(null);
        detailDTO.setHostingId(hostingId);
        detailDTO.setInvoiceId(invoiceId);
        detailDTO.setCcEmails("cc1@geobill.test,cc2@geobill.test");
        detailDTO.setCreatedDate("07-09-2026 08:00");
        detailDTO.setLastReply("07-09-2026 10:30");
        detailDTO.setClosedDate("08-09-2026 09:00");
        detailDTO.setRating(5);
        detailDTO.setFlagged(true);

        Assertions.assertEquals("TKT-202609-000001", detailDTO.getNumber());
        Assertions.assertEquals("Website down", detailDTO.getSubject());
        Assertions.assertEquals("URGENT", detailDTO.getPriority());
        Assertions.assertEquals("awaiting_staff", detailDTO.getStatus());
        Assertions.assertEquals("WEB", detailDTO.getSource());
        Assertions.assertEquals("Technical Support", detailDTO.getDepartment());
        Assertions.assertEquals(departmentId, detailDTO.getDepartmentId());
        Assertions.assertEquals("Client One", detailDTO.getRequester());
        Assertions.assertEquals(requesterId, detailDTO.getRequesterId());
        Assertions.assertEquals("Unassigned", detailDTO.getAssignedTo());
        Assertions.assertNull(detailDTO.getAssignedToId());
        Assertions.assertEquals(hostingId, detailDTO.getHostingId());
        Assertions.assertEquals(invoiceId, detailDTO.getInvoiceId());
        Assertions.assertEquals("cc1@geobill.test,cc2@geobill.test", detailDTO.getCcEmails());
        Assertions.assertEquals("07-09-2026 08:00", detailDTO.getCreatedDate());
        Assertions.assertEquals("07-09-2026 10:30", detailDTO.getLastReply());
        Assertions.assertEquals("08-09-2026 09:00", detailDTO.getClosedDate());
        Assertions.assertEquals(5, detailDTO.getRating());
        Assertions.assertTrue(detailDTO.isFlagged());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Set and Get Replies And Attachments")
    void testSetAndGetCollections() {
        TicketReplyDTO reply = new TicketReplyDTO();
        reply.setMessage("Pesan pertama");
        TicketAttachmentDTO attachment = new TicketAttachmentDTO();
        attachment.setName("error.png");

        detailDTO.setReplies(List.of(reply));
        detailDTO.setAttachments(List.of(attachment));

        Assertions.assertEquals(1, detailDTO.getReplies().size());
        Assertions.assertEquals("Pesan pertama", detailDTO.getReplies().get(0).getMessage());
        Assertions.assertEquals(1, detailDTO.getAttachments().size());
        Assertions.assertEquals("error.png", detailDTO.getAttachments().get(0).getName());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Default Value")
    void testDefaultValue() {
        Assertions.assertNull(detailDTO.getId());
        Assertions.assertNull(detailDTO.getReplies());
        Assertions.assertNull(detailDTO.getAttachments());
        Assertions.assertNull(detailDTO.getRating());
        Assertions.assertFalse(detailDTO.isFlagged());
    }
}
