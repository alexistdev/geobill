package com.alexistdev.geobill.models.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketAttachmentTest {

    private UUID id;
    private UUID ticketId;
    private UUID replyId;
    private UUID uploaderId;
    private TicketAttachment attachment;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        replyId = UUID.randomUUID();
        uploaderId = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);

        TicketReply reply = new TicketReply();
        reply.setId(replyId);

        User uploader = new User();
        uploader.setId(uploaderId);

        attachment = new TicketAttachment();
        attachment.setId(id);
        attachment.setTicket(ticket);
        attachment.setReply(reply);
        attachment.setOriginalName("error.png");
        attachment.setStoredName("a1b2c3d4.png");
        attachment.setStoragePath("/var/geobill/tickets/a1b2c3d4.png");
        attachment.setMimeType("image/png");
        attachment.setFileSize(20480L);
        attachment.setUploadedBy(uploader);
        attachment.setDeleted(false);
        attachment.setCreatedBy("system");
        attachment.setCreatedDate(new Date());
        attachment.setModifiedBy("system");
        attachment.setModifiedDate(new Date());
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get data")
    void testGetData() {
        Assertions.assertEquals(id, attachment.getId());
        Assertions.assertEquals(ticketId, attachment.getTicket().getId());
        Assertions.assertEquals(replyId, attachment.getReply().getId());
        Assertions.assertEquals("error.png", attachment.getOriginalName());
        Assertions.assertEquals("a1b2c3d4.png", attachment.getStoredName());
        Assertions.assertEquals("/var/geobill/tickets/a1b2c3d4.png", attachment.getStoragePath());
        Assertions.assertEquals("image/png", attachment.getMimeType());
        Assertions.assertEquals(20480L, attachment.getFileSize());
        Assertions.assertEquals(uploaderId, attachment.getUploadedBy().getId());
        Assertions.assertNotNull(attachment.getCreatedDate());
        Assertions.assertNotNull(attachment.getModifiedDate());
        Assertions.assertNotNull(attachment.getCreatedBy());
        Assertions.assertNotNull(attachment.getModifiedBy());
        Assertions.assertFalse(attachment.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        UUID newTicketId = UUID.randomUUID();
        Ticket newTicket = new Ticket();
        newTicket.setId(newTicketId);

        attachment.setId(newId);
        attachment.setTicket(newTicket);
        attachment.setOriginalName("invoice.pdf");
        attachment.setStoredName("f9e8d7c6.pdf");
        attachment.setStoragePath("/var/geobill/tickets/f9e8d7c6.pdf");
        attachment.setMimeType("application/pdf");
        attachment.setFileSize(1048576L);

        Assertions.assertEquals(newId, attachment.getId());
        Assertions.assertEquals(newTicketId, attachment.getTicket().getId());
        Assertions.assertEquals("invoice.pdf", attachment.getOriginalName());
        Assertions.assertEquals("f9e8d7c6.pdf", attachment.getStoredName());
        Assertions.assertEquals("/var/geobill/tickets/f9e8d7c6.pdf", attachment.getStoragePath());
        Assertions.assertEquals("application/pdf", attachment.getMimeType());
        Assertions.assertEquals(1048576L, attachment.getFileSize());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure TicketAttachment is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, attachment);
    }

    @Test
    @Order(4)
    @DisplayName("4. Should verify TicketAttachment extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, attachment);
    }

    @Test
    @Order(5)
    @DisplayName("5. Should verify id is UUID")
    void testIdTypeIsUUID() {
        try {
            Field field = TicketAttachment.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException e) {
            Assertions.fail("Field 'id' not found in TicketAttachment class");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Should not allow null value")
    void testNullNotAllowed() {
        attachment.setTicket(null);
        attachment.setOriginalName(null);
        attachment.setStoredName(null);
        attachment.setStoragePath(null);
        attachment.setMimeType(null);
        attachment.setFileSize(null);

        Set<ConstraintViolation<TicketAttachment>> violations = validator.validate(attachment);
        Assertions.assertEquals(6, violations.size(),
                "Should have exactly 6 validation errors for null mandatory fields");

        List<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        Assertions.assertTrue(violatedProperties.contains("ticket"), "Missing violation for 'ticket'");
        Assertions.assertTrue(violatedProperties.contains("originalName"), "Missing violation for 'originalName'");
        Assertions.assertTrue(violatedProperties.contains("storedName"), "Missing violation for 'storedName'");
        Assertions.assertTrue(violatedProperties.contains("storagePath"), "Missing violation for 'storagePath'");
        Assertions.assertTrue(violatedProperties.contains("mimeType"), "Missing violation for 'mimeType'");
        Assertions.assertTrue(violatedProperties.contains("fileSize"), "Missing violation for 'fileSize'");
    }

    @Test
    @Order(7)
    @DisplayName("7. Lampiran boleh menempel ke tiket tanpa balasan")
    void testAttachmentWithoutReply() {
        attachment.setReply(null);
        attachment.setUploadedBy(null);

        Set<ConstraintViolation<TicketAttachment>> violations = validator.validate(attachment);
        Assertions.assertTrue(violations.isEmpty());
        Assertions.assertNotNull(attachment.getTicket(),
                "ticket tetap wajib walau lampiran tidak menempel pada balasan");
    }

    @Test
    @Order(8)
    @DisplayName("8. Nama simpan harus berbeda dari nama asli")
    void testStoredNameDiffersFromOriginalName() {
        Assertions.assertNotEquals(attachment.getOriginalName(), attachment.getStoredName(),
                "Nama acak di penyimpanan memisahkan file dari nama kiriman klien");
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Equals and HashCode")
    void testEqualsAndHashCode() {
        TicketAttachment sameId = new TicketAttachment();
        sameId.setId(attachment.getId());

        TicketAttachment otherId = new TicketAttachment();
        otherId.setId(UUID.randomUUID());

        Assertions.assertEquals(attachment, sameId);
        Assertions.assertEquals(attachment.hashCode(), sameId.hashCode());
        Assertions.assertNotEquals(attachment, otherId);
    }
}
