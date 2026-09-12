package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class TicketHistoryRepoTest {

    private static final long ONE_MINUTE = 60L * 1000;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketHistoryRepo historyRepo;

    private Ticket ticket;
    private Ticket otherTicket;
    private User staff;
    private Date baseTime;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        baseTime = new Date();

        User client = new User();
        client.setFullName("Client One");
        client.setEmail("client1@geobill.test");
        client.setPassword("password");
        client.setRole(Role.USER);
        entityManager.persist(client);

        staff = new User();
        staff.setFullName("Staff One");
        staff.setEmail("staff1@geobill.test");
        staff.setPassword("password");
        staff.setRole(Role.STAFF);
        entityManager.persist(staff);

        TicketDepartment department = new TicketDepartment();
        department.setName("Technical Support");
        department.setCode("TECH");
        entityManager.persist(department);

        ticket = new Ticket();
        ticket.setTicketNumber("TKT-202609-000001");
        ticket.setSubject("Website down");
        ticket.setUser(client);
        ticket.setDepartment(department);
        entityManager.persist(ticket);

        otherTicket = new Ticket();
        otherTicket.setTicketNumber("TKT-202609-000002");
        otherTicket.setSubject("Email tidak masuk");
        otherTicket.setUser(client);
        otherTicket.setDepartment(department);
        entityManager.persist(otherTicket);

        entityManager.flush();
    }

    private TicketHistory createHistory(Ticket target, User actor, TicketHistoryAction action,
                                        String oldValue, String newValue) {
        TicketHistory history = new TicketHistory();
        history.setTicket(target);
        history.setActor(actor);
        history.setAction(action);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        return entityManager.persist(history);
    }

    /** Auditing mengisi createdDate dengan waktu yang sama, jadi urutannya ditimpa langsung. */
    private void setCreatedDate(UUID historyId, long offsetMinutes) {
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE tb_ticket_histories SET created_date = ?1 WHERE uuid = ?2")
                .setParameter(1, new Timestamp(baseTime.getTime() + offsetMinutes * ONE_MINUTE))
                .setParameter(2, historyId)
                .executeUpdate();
    }

    /** Riwayat satu tiket yang dibuang dua kali, agar entri TRASHED terakhir bisa diuji. */
    private void seedHistory() {
        TicketHistory created = createHistory(ticket, null, TicketHistoryAction.CREATED, null,
                TicketStatus.AWAITING_STAFF.name());
        TicketHistory firstTrash = createHistory(ticket, staff, TicketHistoryAction.TRASHED,
                TicketStatus.IN_PROGRESS.name(), TicketStatus.TRASH.name());
        TicketHistory restored = createHistory(ticket, staff, TicketHistoryAction.RESTORED,
                TicketStatus.TRASH.name(), TicketStatus.IN_PROGRESS.name());
        TicketHistory lastTrash = createHistory(ticket, staff, TicketHistoryAction.TRASHED,
                TicketStatus.ON_HOLD.name(), TicketStatus.TRASH.name());
        createHistory(otherTicket, staff, TicketHistoryAction.ASSIGNED, null, "Staff One");
        entityManager.flush();

        setCreatedDate(created.getId(), 0);
        setCreatedDate(firstTrash.getId(), 1);
        setCreatedDate(restored.getId(), 2);
        setCreatedDate(lastTrash.getId(), 3);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save History")
    void testSaveHistory() {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setActor(staff);
        history.setAction(TicketHistoryAction.STATUS_CHANGED);
        history.setOldValue(TicketStatus.AWAITING_STAFF.name());
        history.setNewValue(TicketStatus.IN_PROGRESS.name());

        TicketHistory saved = historyRepo.save(history);

        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(ticket.getId(), saved.getTicket().getId());
        Assertions.assertEquals(TicketHistoryAction.STATUS_CHANGED, saved.getAction());
        Assertions.assertEquals("AWAITING_STAFF", saved.getOldValue());
        Assertions.assertFalse(saved.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Riwayat Satu Tiket Urut Dari Yang Terbaru")
    void testFindByTicketId() {
        seedHistory();

        List<TicketHistory> histories = historyRepo.findByTicketId(ticket.getId());

        Assertions.assertEquals(4, histories.size());
        Assertions.assertEquals(TicketHistoryAction.TRASHED, histories.get(0).getAction());
        Assertions.assertEquals(TicketHistoryAction.CREATED, histories.get(3).getAction());
        Assertions.assertEquals(4, historyRepo.countByTicket_Id(ticket.getId()));
        Assertions.assertEquals(1, historyRepo.countByTicket_Id(otherTicket.getId()));
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Find By Action")
    void testFindByAction() {
        seedHistory();

        Assertions.assertEquals(2, historyRepo.findByTicket_IdAndAction(ticket.getId(),
                TicketHistoryAction.TRASHED).size());
        Assertions.assertEquals(1, historyRepo.findByTicket_IdAndAction(ticket.getId(),
                TicketHistoryAction.RESTORED).size());
        Assertions.assertEquals(0, historyRepo.findByTicket_IdAndAction(ticket.getId(),
                TicketHistoryAction.CLOSED).size());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Entri TRASHED Terakhir Menyimpan Status Untuk Restore")
    void testFindLastTrashedForRestore() {
        seedHistory();

        TicketHistory lastTrash = historyRepo
                .findFirstByTicket_IdAndActionOrderByCreatedDateDesc(ticket.getId(), TicketHistoryAction.TRASHED)
                .orElseThrow();

        Assertions.assertEquals(TicketStatus.ON_HOLD, TicketStatus.valueOf(lastTrash.getOldValue()),
                "Restore harus memakai status sebelum pembuangan terakhir, bukan yang pertama");
        Assertions.assertEquals(TicketStatus.TRASH, TicketStatus.valueOf(lastTrash.getNewValue()));
        Assertions.assertTrue(historyRepo.findFirstByTicket_IdAndActionOrderByCreatedDateDesc(
                UUID.randomUUID(), TicketHistoryAction.TRASHED).isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Halaman Riwayat")
    void testFindPaged() {
        seedHistory();

        Assertions.assertEquals(4, historyRepo
                .findByTicket_IdOrderByCreatedDateDesc(ticket.getId(), PageRequest.of(0, 10)).getTotalElements());
        Assertions.assertEquals(TicketHistoryAction.TRASHED, historyRepo
                .findByTicket_IdOrderByCreatedDateDesc(ticket.getId(), PageRequest.of(0, 1))
                .getContent().get(0).getAction());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Aksi Sistem Tersimpan Tanpa Actor")
    void testSystemActionWithoutActor() {
        historyRepo.save(createSystemClose());
        entityManager.flush();
        entityManager.clear();

        TicketHistory saved = historyRepo.findByTicketId(ticket.getId()).get(0);

        Assertions.assertNull(saved.getActor(), "Auto close dijalankan sistem, tanpa actor");
        Assertions.assertEquals(TicketHistoryAction.CLOSED, saved.getAction());
    }

    private TicketHistory createSystemClose() {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setAction(TicketHistoryAction.CLOSED);
        history.setOldValue(TicketStatus.AWAITING_CLIENT.name());
        history.setNewValue(TicketStatus.CLOSED.name());
        history.setNote("Ditutup otomatis karena klien tidak membalas");
        return history;
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Delete History")
    void testDeleteHistory() {
        TicketHistory history = createHistory(ticket, staff, TicketHistoryAction.ASSIGNED, null, "Staff One");
        entityManager.flush();

        historyRepo.delete(history);
        entityManager.flush();
        entityManager.clear();

        Assertions.assertTrue(historyRepo.findById(history.getId()).isEmpty());
        Assertions.assertEquals(0, historyRepo.countByTicket_Id(ticket.getId()));

        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_ticket_histories WHERE uuid = ?1")
                .setParameter(1, history.getId())
                .getSingleResult();

        Assertions.assertTrue(isDeleted instanceof Boolean ? (Boolean) isDeleted : ((Number) isDeleted).intValue() == 1,
                "History should be soft-deleted in the database");
    }
}
