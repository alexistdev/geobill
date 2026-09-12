package com.alexistdev.geobill.dto.ticket_system;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketReplyDTOTest {

    private TicketReplyDTO replyDTO;

    @BeforeEach
    void setUp() {
        replyDTO = new TicketReplyDTO();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Set and Get Id")
    void testSetAndGetId() {
        String id = UUID.randomUUID().toString();
        replyDTO.setId(id);
        Assertions.assertEquals(id, replyDTO.getId());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set and Get")
    void testSetAndGet() {
        String ticketId = UUID.randomUUID().toString();
        String authorId = UUID.randomUUID().toString();

        replyDTO.setTicketId(ticketId);
        replyDTO.setAuthorType("CLIENT");
        replyDTO.setAuthorName("Client One");
        replyDTO.setAuthorId(authorId);
        replyDTO.setMessage("Situs saya error 500");
        replyDTO.setInternalNote(false);
        replyDTO.setCreatedDate("07-09-2026 08:00");

        Assertions.assertEquals(ticketId, replyDTO.getTicketId());
        Assertions.assertEquals("CLIENT", replyDTO.getAuthorType());
        Assertions.assertEquals("Client One", replyDTO.getAuthorName());
        Assertions.assertEquals(authorId, replyDTO.getAuthorId());
        Assertions.assertEquals("Situs saya error 500", replyDTO.getMessage());
        Assertions.assertFalse(replyDTO.isInternalNote());
        Assertions.assertEquals("07-09-2026 08:00", replyDTO.getCreatedDate());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Internal Note Flag")
    void testInternalNoteFlag() {
        replyDTO.setAuthorType("STAFF");
        replyDTO.setInternalNote(true);

        Assertions.assertTrue(replyDTO.isInternalNote());
        Assertions.assertEquals("STAFF", replyDTO.getAuthorType());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test System Reply Has No Author Id")
    void testSystemReply() {
        replyDTO.setAuthorType("SYSTEM");
        replyDTO.setAuthorId(null);
        replyDTO.setAuthorName(null);

        Assertions.assertEquals("SYSTEM", replyDTO.getAuthorType());
        Assertions.assertNull(replyDTO.getAuthorId());
        Assertions.assertNull(replyDTO.getAuthorName());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Set and Get Attachments")
    void testSetAndGetAttachments() {
        TicketAttachmentDTO attachment = new TicketAttachmentDTO();
        attachment.setName("error.png");
        replyDTO.setAttachments(List.of(attachment));

        Assertions.assertEquals(1, replyDTO.getAttachments().size());
        Assertions.assertEquals("error.png", replyDTO.getAttachments().get(0).getName());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Default Value")
    void testDefaultValue() {
        Assertions.assertNull(replyDTO.getId());
        Assertions.assertNull(replyDTO.getAttachments());
        Assertions.assertFalse(replyDTO.isInternalNote());
    }
}
