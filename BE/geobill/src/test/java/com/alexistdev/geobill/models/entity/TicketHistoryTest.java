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
public class TicketHistoryTest {

    private UUID id;
    private UUID ticketId;
    private UUID actorId;
    private TicketHistory history;
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
        actorId = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);

        User actor = new User();
        actor.setId(actorId);

        history = new TicketHistory();
        history.setId(id);
        history.setTicket(ticket);
        history.setActor(actor);
        history.setAction(TicketHistoryAction.STATUS_CHANGED);
        history.setOldValue(TicketStatus.AWAITING_STAFF.name());
        history.setNewValue(TicketStatus.IN_PROGRESS.name());
        history.setNote("Staff mulai menangani tiket");
        history.setDeleted(false);
        history.setCreatedBy("system");
        history.setCreatedDate(new Date());
        history.setModifiedBy("system");
        history.setModifiedDate(new Date());
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get data")
    void testGetData() {
        Assertions.assertEquals(id, history.getId());
        Assertions.assertEquals(ticketId, history.getTicket().getId());
        Assertions.assertEquals(actorId, history.getActor().getId());
        Assertions.assertEquals(TicketHistoryAction.STATUS_CHANGED, history.getAction());
        Assertions.assertEquals("AWAITING_STAFF", history.getOldValue());
        Assertions.assertEquals("IN_PROGRESS", history.getNewValue());
        Assertions.assertEquals("Staff mulai menangani tiket", history.getNote());
        Assertions.assertNotNull(history.getCreatedDate());
        Assertions.assertNotNull(history.getModifiedDate());
        Assertions.assertNotNull(history.getCreatedBy());
        Assertions.assertNotNull(history.getModifiedBy());
        Assertions.assertFalse(history.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        UUID newTicketId = UUID.randomUUID();
        Ticket newTicket = new Ticket();
        newTicket.setId(newTicketId);

        history.setId(newId);
        history.setTicket(newTicket);
        history.setAction(TicketHistoryAction.ASSIGNED);
        history.setOldValue(null);
        history.setNewValue("Staff One");
        history.setNote(null);

        Assertions.assertEquals(newId, history.getId());
        Assertions.assertEquals(newTicketId, history.getTicket().getId());
        Assertions.assertEquals(TicketHistoryAction.ASSIGNED, history.getAction());
        Assertions.assertNull(history.getOldValue());
        Assertions.assertEquals("Staff One", history.getNewValue());
        Assertions.assertNull(history.getNote());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure TicketHistory is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, history);
    }

    @Test
    @Order(4)
    @DisplayName("4. Should verify TicketHistory extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, history);
    }

    @Test
    @Order(5)
    @DisplayName("5. Should verify id is UUID")
    void testIdTypeIsUUID() {
        try {
            Field field = TicketHistory.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException e) {
            Assertions.fail("Field 'id' not found in TicketHistory class");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Should not allow null value")
    void testNullNotAllowed() {
        history.setTicket(null);
        history.setAction(null);

        Set<ConstraintViolation<TicketHistory>> violations = validator.validate(history);
        Assertions.assertEquals(2, violations.size(),
                "Should have exactly 2 validation errors for null mandatory fields");

        List<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        Assertions.assertTrue(violatedProperties.contains("ticket"), "Missing violation for 'ticket'");
        Assertions.assertTrue(violatedProperties.contains("action"), "Missing violation for 'action'");
    }

    @Test
    @Order(7)
    @DisplayName("7. Aksi sistem boleh tanpa actor")
    void testSystemActionHasNoActor() {
        history.setActor(null);
        history.setAction(TicketHistoryAction.CLOSED);

        Set<ConstraintViolation<TicketHistory>> violations = validator.validate(history);
        Assertions.assertTrue(violations.isEmpty(),
                "Auto close dijalankan sistem, jadi actor boleh kosong");
    }

    @Test
    @Order(8)
    @DisplayName("8. Riwayat TRASHED menyimpan status sebelumnya untuk restore")
    void testTrashKeepsPreviousStatus() {
        history.setAction(TicketHistoryAction.TRASHED);
        history.setOldValue(TicketStatus.ON_HOLD.name());
        history.setNewValue(TicketStatus.TRASH.name());

        Assertions.assertEquals(TicketStatus.ON_HOLD, TicketStatus.valueOf(history.getOldValue()),
                "Nilai lama harus bisa dibaca balik saat tiket di-restore");
        Assertions.assertEquals(TicketStatus.TRASH, TicketStatus.valueOf(history.getNewValue()));
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Equals and HashCode")
    void testEqualsAndHashCode() {
        TicketHistory sameId = new TicketHistory();
        sameId.setId(history.getId());

        TicketHistory otherId = new TicketHistory();
        otherId.setId(UUID.randomUUID());

        Assertions.assertEquals(history, sameId);
        Assertions.assertEquals(history.hashCode(), sameId.hashCode());
        Assertions.assertNotEquals(history, otherId);
    }
}
