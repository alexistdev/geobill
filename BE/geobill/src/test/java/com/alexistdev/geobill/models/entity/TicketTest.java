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
public class TicketTest {

    private UUID id;
    private UUID userId;
    private UUID staffId;
    private UUID departmentId;
    private UUID hostingId;
    private UUID invoiceId;
    private Ticket ticket;
    private String ticketNumber;
    private String subject;
    private Date lastReplyAt;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        userId = UUID.randomUUID();
        staffId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        hostingId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        ticketNumber = "TKT-202609-000001";
        subject = "Website tidak bisa diakses";
        lastReplyAt = new Date();

        User user = new User();
        user.setId(userId);

        User staff = new User();
        staff.setId(staffId);

        TicketDepartment department = new TicketDepartment();
        department.setId(departmentId);

        Hosting hosting = new Hosting();
        hosting.setId(hostingId);

        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);

        ticket = new Ticket();
        ticket.setId(id);
        ticket.setTicketNumber(ticketNumber);
        ticket.setUser(user);
        ticket.setDepartment(department);
        ticket.setAssignedTo(staff);
        ticket.setHosting(hosting);
        ticket.setInvoice(invoice);
        ticket.setSubject(subject);
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setSource(TicketSource.WEB);
        ticket.setCcEmails("cc1@geobill.test,cc2@geobill.test");
        ticket.setReplyCount(2);
        ticket.setLastReplyAt(lastReplyAt);
        ticket.setLastReplyBy(user);
        ticket.setLastReplyAuthorType(TicketAuthorType.CLIENT);
        ticket.setFirstResponseAt(lastReplyAt);
        ticket.setDueAt(lastReplyAt);
        ticket.setRating(5);
        ticket.setIsFlagged(Boolean.TRUE);
        ticket.setDeleted(false);
        ticket.setCreatedBy("system");
        ticket.setCreatedDate(new Date());
        ticket.setModifiedBy("system");
        ticket.setModifiedDate(new Date());
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get data")
    void testGetData() {
        Assertions.assertNotNull(ticket.getId());
        Assertions.assertEquals(id, ticket.getId());
        Assertions.assertEquals(ticketNumber, ticket.getTicketNumber());
        Assertions.assertEquals(userId, ticket.getUser().getId());
        Assertions.assertEquals(departmentId, ticket.getDepartment().getId());
        Assertions.assertEquals(staffId, ticket.getAssignedTo().getId());
        Assertions.assertEquals(hostingId, ticket.getHosting().getId());
        Assertions.assertEquals(invoiceId, ticket.getInvoice().getId());
        Assertions.assertEquals(subject, ticket.getSubject());
        Assertions.assertEquals(TicketPriority.HIGH, ticket.getPriority());
        Assertions.assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        Assertions.assertEquals(TicketSource.WEB, ticket.getSource());
        Assertions.assertEquals("cc1@geobill.test,cc2@geobill.test", ticket.getCcEmails());
        Assertions.assertEquals(2, ticket.getReplyCount());
        Assertions.assertEquals(lastReplyAt, ticket.getLastReplyAt());
        Assertions.assertEquals(userId, ticket.getLastReplyBy().getId());
        Assertions.assertEquals(TicketAuthorType.CLIENT, ticket.getLastReplyAuthorType());
        Assertions.assertEquals(5, ticket.getRating());
        Assertions.assertTrue(ticket.getIsFlagged());
        Assertions.assertNotNull(ticket.getCreatedDate());
        Assertions.assertNotNull(ticket.getModifiedDate());
        Assertions.assertNotNull(ticket.getCreatedBy());
        Assertions.assertNotNull(ticket.getModifiedBy());
        Assertions.assertFalse(ticket.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        UUID newDepartmentId = UUID.randomUUID();
        User newUser = new User();
        newUser.setId(newUserId);
        TicketDepartment newDepartment = new TicketDepartment();
        newDepartment.setId(newDepartmentId);
        Date closedAt = new Date();

        ticket.setId(newId);
        ticket.setTicketNumber("TKT-202609-000002");
        ticket.setUser(newUser);
        ticket.setDepartment(newDepartment);
        ticket.setSubject("Tagihan ganda");
        ticket.setPriority(TicketPriority.URGENT);
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setSource(TicketSource.EMAIL);
        ticket.setReplyCount(7);
        ticket.setClosedAt(closedAt);
        ticket.setClosedBy(newUser);
        ticket.setIsFlagged(Boolean.FALSE);

        Assertions.assertEquals(newId, ticket.getId());
        Assertions.assertEquals("TKT-202609-000002", ticket.getTicketNumber());
        Assertions.assertEquals(newUserId, ticket.getUser().getId());
        Assertions.assertEquals(newDepartmentId, ticket.getDepartment().getId());
        Assertions.assertEquals("Tagihan ganda", ticket.getSubject());
        Assertions.assertEquals(TicketPriority.URGENT, ticket.getPriority());
        Assertions.assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        Assertions.assertEquals(TicketSource.EMAIL, ticket.getSource());
        Assertions.assertEquals(7, ticket.getReplyCount());
        Assertions.assertEquals(closedAt, ticket.getClosedAt());
        Assertions.assertEquals(newUserId, ticket.getClosedBy().getId());
        Assertions.assertFalse(ticket.getIsFlagged());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure Ticket is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, ticket);
    }

    @Test
    @Order(4)
    @DisplayName("4. Should verify Ticket extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, ticket);
    }

    @Test
    @Order(5)
    @DisplayName("5. Should verify id is UUID")
    void testIdTypeIsUUID() {
        try {
            Field field = Ticket.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException e) {
            Assertions.fail("Field 'id' not found in Ticket class");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Should not allow null value")
    void testNullNotAllowed() {
        ticket.setTicketNumber(null);
        ticket.setUser(null);
        ticket.setDepartment(null);
        ticket.setSubject(null);
        ticket.setPriority(null);
        ticket.setStatus(null);
        ticket.setSource(null);
        ticket.setReplyCount(null);
        ticket.setIsFlagged(null);

        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        Assertions.assertEquals(9, violations.size(),
                "Should have exactly 9 validation errors for null mandatory fields");

        List<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        Assertions.assertTrue(violatedProperties.contains("ticketNumber"), "Missing violation for 'ticketNumber'");
        Assertions.assertTrue(violatedProperties.contains("user"), "Missing violation for 'user'");
        Assertions.assertTrue(violatedProperties.contains("department"), "Missing violation for 'department'");
        Assertions.assertTrue(violatedProperties.contains("subject"), "Missing violation for 'subject'");
        Assertions.assertTrue(violatedProperties.contains("priority"), "Missing violation for 'priority'");
        Assertions.assertTrue(violatedProperties.contains("status"), "Missing violation for 'status'");
        Assertions.assertTrue(violatedProperties.contains("source"), "Missing violation for 'source'");
        Assertions.assertTrue(violatedProperties.contains("replyCount"), "Missing violation for 'replyCount'");
        Assertions.assertTrue(violatedProperties.contains("isFlagged"), "Missing violation for 'isFlagged'");
    }

    @Test
    @Order(7)
    @DisplayName("7. Nomor tiket dan subject kosong harus ditolak")
    void testBlankNotAllowed() {
        ticket.setTicketNumber("   ");
        ticket.setSubject("");

        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        List<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        Assertions.assertEquals(2, violations.size());
        Assertions.assertTrue(violatedProperties.contains("ticketNumber"));
        Assertions.assertTrue(violatedProperties.contains("subject"));
    }

    @Test
    @Order(8)
    @DisplayName("8. Relasi opsional boleh kosong")
    void testOptionalRelationsMayBeNull() {
        ticket.setAssignedTo(null);
        ticket.setHosting(null);
        ticket.setInvoice(null);
        ticket.setLastReplyBy(null);
        ticket.setLastReplyAt(null);
        ticket.setLastReplyAuthorType(null);
        ticket.setClosedBy(null);
        ticket.setClosedAt(null);
        ticket.setCcEmails(null);
        ticket.setRating(null);

        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        Assertions.assertTrue(violations.isEmpty(),
                "Tiket tanpa assignee, hosting, atau invoice tetap valid");
    }

    @Test
    @Order(9)
    @DisplayName("9. Nilai default tiket baru")
    void testDefaultValues() {
        Ticket fresh = new Ticket();

        Assertions.assertEquals(TicketStatus.AWAITING_STAFF, fresh.getStatus(),
                "Tiket baru menunggu staff");
        Assertions.assertEquals(TicketPriority.MEDIUM, fresh.getPriority());
        Assertions.assertEquals(TicketSource.WEB, fresh.getSource());
        Assertions.assertEquals(0, fresh.getReplyCount());
        Assertions.assertFalse(fresh.getIsFlagged());
        Assertions.assertFalse(fresh.getDeleted());
        Assertions.assertNull(fresh.getAssignedTo());
        Assertions.assertNull(fresh.getLastReplyAt());
        Assertions.assertNull(fresh.getClosedAt());
    }

    @Test
    @Order(10)
    @DisplayName("10. Tiket di Trash tidak ikut ter-soft-delete")
    void testTrashIsNotSoftDelete() {
        ticket.setStatus(TicketStatus.TRASH);

        Assertions.assertEquals(TicketStatus.TRASH, ticket.getStatus());
        Assertions.assertFalse(ticket.getDeleted(),
                "Status TRASH harus terpisah dari is_deleted supaya tiket masih bisa di-restore");
    }

    @Test
    @Order(11)
    @DisplayName("11. Test Equals and HashCode")
    void testEqualsAndHashCode() {
        Ticket sameId = new Ticket();
        sameId.setId(ticket.getId());
        sameId.setSubject("Subject lain");

        Ticket otherId = new Ticket();
        otherId.setId(UUID.randomUUID());

        Assertions.assertEquals(ticket, sameId, "Ticket with same ID should be equal");
        Assertions.assertEquals(ticket.hashCode(), sameId.hashCode(), "HashCodes should match for same ID");
        Assertions.assertNotEquals(ticket, otherId, "Ticket with different ID should not be equal");
    }
}
