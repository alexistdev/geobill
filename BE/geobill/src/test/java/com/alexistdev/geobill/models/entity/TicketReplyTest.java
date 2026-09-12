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
public class TicketReplyTest {

    private UUID id;
    private UUID ticketId;
    private UUID userId;
    private TicketReply reply;
    private String message;
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
        userId = UUID.randomUUID();
        message = "Situs saya mengembalikan error 500 sejak pagi ini.";

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);

        User user = new User();
        user.setId(userId);
        user.setFullName("Client One");

        reply = new TicketReply();
        reply.setId(id);
        reply.setTicket(ticket);
        reply.setUser(user);
        reply.setAuthorType(TicketAuthorType.CLIENT);
        reply.setAuthorName("Client One");
        reply.setMessage(message);
        reply.setIsInternalNote(Boolean.FALSE);
        reply.setIsEmailSent(Boolean.TRUE);
        reply.setIpAddress("2001:db8::1");
        reply.setDeleted(false);
        reply.setCreatedBy("system");
        reply.setCreatedDate(new Date());
        reply.setModifiedBy("system");
        reply.setModifiedDate(new Date());
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get data")
    void testGetData() {
        Assertions.assertEquals(id, reply.getId());
        Assertions.assertEquals(ticketId, reply.getTicket().getId());
        Assertions.assertEquals(userId, reply.getUser().getId());
        Assertions.assertEquals(TicketAuthorType.CLIENT, reply.getAuthorType());
        Assertions.assertEquals("Client One", reply.getAuthorName());
        Assertions.assertEquals(message, reply.getMessage());
        Assertions.assertFalse(reply.getIsInternalNote());
        Assertions.assertTrue(reply.getIsEmailSent());
        Assertions.assertEquals("2001:db8::1", reply.getIpAddress());
        Assertions.assertNotNull(reply.getCreatedDate());
        Assertions.assertNotNull(reply.getModifiedDate());
        Assertions.assertNotNull(reply.getCreatedBy());
        Assertions.assertNotNull(reply.getModifiedBy());
        Assertions.assertFalse(reply.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        UUID newTicketId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();

        Ticket newTicket = new Ticket();
        newTicket.setId(newTicketId);
        User staff = new User();
        staff.setId(staffId);

        reply.setId(newId);
        reply.setTicket(newTicket);
        reply.setUser(staff);
        reply.setAuthorType(TicketAuthorType.STAFF);
        reply.setAuthorName("Staff One");
        reply.setMessage("Sudah kami cek, mohon tunggu.");
        reply.setIsInternalNote(Boolean.TRUE);
        reply.setIsEmailSent(Boolean.FALSE);
        reply.setIpAddress("192.168.1.10");

        Assertions.assertEquals(newId, reply.getId());
        Assertions.assertEquals(newTicketId, reply.getTicket().getId());
        Assertions.assertEquals(staffId, reply.getUser().getId());
        Assertions.assertEquals(TicketAuthorType.STAFF, reply.getAuthorType());
        Assertions.assertEquals("Staff One", reply.getAuthorName());
        Assertions.assertEquals("Sudah kami cek, mohon tunggu.", reply.getMessage());
        Assertions.assertTrue(reply.getIsInternalNote());
        Assertions.assertFalse(reply.getIsEmailSent());
        Assertions.assertEquals("192.168.1.10", reply.getIpAddress());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure TicketReply is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, reply);
    }

    @Test
    @Order(4)
    @DisplayName("4. Should verify TicketReply extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, reply);
    }

    @Test
    @Order(5)
    @DisplayName("5. Should verify id is UUID")
    void testIdTypeIsUUID() {
        try {
            Field field = TicketReply.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException e) {
            Assertions.fail("Field 'id' not found in TicketReply class");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Should not allow null value")
    void testNullNotAllowed() {
        reply.setTicket(null);
        reply.setAuthorType(null);
        reply.setMessage(null);
        reply.setIsInternalNote(null);
        reply.setIsEmailSent(null);

        Set<ConstraintViolation<TicketReply>> violations = validator.validate(reply);
        Assertions.assertEquals(5, violations.size(),
                "Should have exactly 5 validation errors for null mandatory fields");

        List<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        Assertions.assertTrue(violatedProperties.contains("ticket"), "Missing violation for 'ticket'");
        Assertions.assertTrue(violatedProperties.contains("authorType"), "Missing violation for 'authorType'");
        Assertions.assertTrue(violatedProperties.contains("message"), "Missing violation for 'message'");
        Assertions.assertTrue(violatedProperties.contains("isInternalNote"), "Missing violation for 'isInternalNote'");
        Assertions.assertTrue(violatedProperties.contains("isEmailSent"), "Missing violation for 'isEmailSent'");
    }

    @Test
    @Order(7)
    @DisplayName("7. Pesan kosong harus ditolak")
    void testBlankMessageNotAllowed() {
        reply.setMessage("   ");

        Set<ConstraintViolation<TicketReply>> violations = validator.validate(reply);
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("message", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @Order(8)
    @DisplayName("8. Balasan sistem boleh tanpa user")
    void testSystemReplyHasNoUser() {
        reply.setUser(null);
        reply.setAuthorType(TicketAuthorType.SYSTEM);
        reply.setAuthorName(null);
        reply.setIpAddress(null);

        Set<ConstraintViolation<TicketReply>> violations = validator.validate(reply);
        Assertions.assertTrue(violations.isEmpty(),
                "Balasan otomatis tidak punya user, dan itu tetap valid");
    }

    @Test
    @Order(9)
    @DisplayName("9. Nilai default balasan baru")
    void testDefaultValues() {
        TicketReply fresh = new TicketReply();

        Assertions.assertFalse(fresh.getIsInternalNote(), "Balasan baru bukan catatan internal");
        Assertions.assertFalse(fresh.getIsEmailSent());
        Assertions.assertFalse(fresh.getDeleted());
        Assertions.assertNull(fresh.getAuthorType());
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Equals and HashCode")
    void testEqualsAndHashCode() {
        TicketReply sameId = new TicketReply();
        sameId.setId(reply.getId());

        TicketReply otherId = new TicketReply();
        otherId.setId(UUID.randomUUID());

        Assertions.assertEquals(reply, sameId);
        Assertions.assertEquals(reply.hashCode(), sameId.hashCode());
        Assertions.assertNotEquals(reply, otherId);
    }
}
