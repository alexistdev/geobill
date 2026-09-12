package com.alexistdev.geobill.dto.ticket_system;

import org.junit.jupiter.api.*;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketAttachmentDTOTest {

    private TicketAttachmentDTO attachmentDTO;

    @BeforeEach
    void setUp() {
        attachmentDTO = new TicketAttachmentDTO();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Set and Get Id")
    void testSetAndGetId() {
        String id = UUID.randomUUID().toString();
        attachmentDTO.setId(id);
        Assertions.assertEquals(id, attachmentDTO.getId());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set and Get")
    void testSetAndGet() {
        String ticketId = UUID.randomUUID().toString();
        String replyId = UUID.randomUUID().toString();

        attachmentDTO.setTicketId(ticketId);
        attachmentDTO.setReplyId(replyId);
        attachmentDTO.setName("error.png");
        attachmentDTO.setMimeType("image/png");
        attachmentDTO.setFileSize(20480L);
        attachmentDTO.setUploadedBy("Client One");
        attachmentDTO.setCreatedDate("07-09-2026 08:00");

        Assertions.assertEquals(ticketId, attachmentDTO.getTicketId());
        Assertions.assertEquals(replyId, attachmentDTO.getReplyId());
        Assertions.assertEquals("error.png", attachmentDTO.getName());
        Assertions.assertEquals("image/png", attachmentDTO.getMimeType());
        Assertions.assertEquals(20480L, attachmentDTO.getFileSize());
        Assertions.assertEquals("Client One", attachmentDTO.getUploadedBy());
        Assertions.assertEquals("07-09-2026 08:00", attachmentDTO.getCreatedDate());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Attachment On Ticket Without Reply")
    void testAttachmentWithoutReply() {
        attachmentDTO.setTicketId(UUID.randomUUID().toString());
        attachmentDTO.setReplyId(null);

        Assertions.assertNotNull(attachmentDTO.getTicketId());
        Assertions.assertNull(attachmentDTO.getReplyId());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Only The Original Name Is Exposed")
    void testOnlyOriginalNameExposed() {
        attachmentDTO.setName("error.png");

        Assertions.assertEquals("error.png", attachmentDTO.getName());
        Assertions.assertEquals(0, java.util.Arrays.stream(TicketAttachmentDTO.class.getDeclaredFields())
                        .filter(field -> field.getName().equals("storagePath") || field.getName().equals("storedName"))
                        .count(),
                "Lokasi file di penyimpanan tidak boleh ikut terkirim ke klien");
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Default Value")
    void testDefaultValue() {
        Assertions.assertNull(attachmentDTO.getId());
        Assertions.assertNull(attachmentDTO.getFileSize());
        Assertions.assertNull(attachmentDTO.getUploadedBy());
    }
}
