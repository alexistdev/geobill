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
public class TicketReplyRepoTest {

    private static final long ONE_MINUTE = 60L * 1000;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketReplyRepo replyRepo;

    private Ticket ticket;
    private Ticket otherTicket;
    private User client;
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
        client = createUser("Client One", "client1@geobill.test", Role.USER);
        staff = createUser("Staff One", "staff1@geobill.test", Role.STAFF);

        TicketDepartment department = new TicketDepartment();
        department.setName("Technical Support");
        department.setCode("TECH");
        entityManager.persist(department);

        ticket = createTicket("TKT-202609-000001", department);
        otherTicket = createTicket("TKT-202609-000002", department);
        entityManager.flush();
    }

    private User createUser(String fullName, String email, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        return entityManager.persist(user);
    }

    private Ticket createTicket(String number, TicketDepartment department) {
        Ticket newTicket = new Ticket();
        newTicket.setTicketNumber(number);
        newTicket.setSubject("Subject " + number);
        newTicket.setUser(client);
        newTicket.setDepartment(department);
        return entityManager.persist(newTicket);
    }

    private TicketReply createReply(Ticket target, User author, TicketAuthorType authorType, String message,
                                    boolean internalNote, boolean emailSent) {
        TicketReply reply = new TicketReply();
        reply.setTicket(target);
        reply.setUser(author);
        reply.setAuthorType(authorType);
        reply.setAuthorName(author == null ? null : author.getFullName());
        reply.setMessage(message);
        reply.setIsInternalNote(internalNote);
        reply.setIsEmailSent(emailSent);
        return entityManager.persist(reply);
    }

    /**
     * JPA auditing mengisi createdDate dengan waktu saat ini, sehingga beberapa balasan yang
     * disimpan berurutan bisa memiliki nilai yang sama persis. Urutan dibuat pasti dengan
     * menimpa kolomnya langsung.
     */
    private void setCreatedDate(UUID replyId, long offsetMinutes) {
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE tb_ticket_replies SET created_date = ?1 WHERE uuid = ?2")
                .setParameter(1, new Timestamp(baseTime.getTime() + offsetMinutes * ONE_MINUTE))
                .setParameter(2, replyId)
                .executeUpdate();
    }

    /** Tiga balasan berurutan: pesan klien, catatan internal staff, lalu jawaban staff. */
    private void seedConversation() {
        TicketReply first = createReply(ticket, client, TicketAuthorType.CLIENT, "Pesan pertama", false, true);
        TicketReply note = createReply(ticket, staff, TicketAuthorType.STAFF, "Catatan internal", true, false);
        TicketReply answer = createReply(ticket, staff, TicketAuthorType.STAFF, "Sedang kami cek", false, false);
        createReply(otherTicket, client, TicketAuthorType.CLIENT, "Tiket lain", false, true);
        entityManager.flush();

        setCreatedDate(first.getId(), 0);
        setCreatedDate(note.getId(), 1);
        setCreatedDate(answer.getId(), 2);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save Reply")
    void testSaveReply() {
        TicketReply reply = new TicketReply();
        reply.setTicket(ticket);
        reply.setUser(client);
        reply.setAuthorType(TicketAuthorType.CLIENT);
        reply.setMessage("Situs saya error 500");

        TicketReply saved = replyRepo.save(reply);

        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(ticket.getId(), saved.getTicket().getId());
        Assertions.assertEquals(TicketAuthorType.CLIENT, saved.getAuthorType());
        Assertions.assertFalse(saved.getIsInternalNote());
        Assertions.assertFalse(saved.getIsEmailSent());
        Assertions.assertFalse(saved.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Percakapan Versi Staff Berisi Catatan Internal")
    void testFindByTicketId() {
        seedConversation();

        List<TicketReply> replies = replyRepo.findByTicketId(ticket.getId());

        Assertions.assertEquals(3, replies.size());
        Assertions.assertEquals("Pesan pertama", replies.get(0).getMessage());
        Assertions.assertEquals("Catatan internal", replies.get(1).getMessage());
        Assertions.assertEquals("Sedang kami cek", replies.get(2).getMessage());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Percakapan Versi Klien Tanpa Catatan Internal")
    void testFindPublicByTicketId() {
        seedConversation();

        List<TicketReply> replies = replyRepo.findPublicByTicketId(ticket.getId());

        Assertions.assertEquals(2, replies.size());
        Assertions.assertTrue(replies.stream().noneMatch(TicketReply::getIsInternalNote),
                "Catatan internal tidak boleh pernah sampai ke klien");
        Assertions.assertEquals("Pesan pertama", replies.get(0).getMessage());
        Assertions.assertEquals("Sedang kami cek", replies.get(1).getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Hitung Balasan Publik Dan Catatan Internal")
    void testCountReplies() {
        seedConversation();

        Assertions.assertEquals(2, replyRepo.countByTicket_IdAndIsInternalNoteFalse(ticket.getId()));
        Assertions.assertEquals(1, replyRepo.countByTicket_IdAndIsInternalNoteTrue(ticket.getId()));
        Assertions.assertEquals(1, replyRepo.countByTicket_IdAndIsInternalNoteFalse(otherTicket.getId()));
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Pesan Pembuka Tiket")
    void testFindFirstReply() {
        seedConversation();

        TicketReply first = replyRepo.findFirstByTicket_IdOrderByCreatedDateAsc(ticket.getId()).orElseThrow();

        Assertions.assertEquals("Pesan pertama", first.getMessage());
        Assertions.assertEquals(TicketAuthorType.CLIENT, first.getAuthorType());
        Assertions.assertTrue(replyRepo.findFirstByTicket_IdOrderByCreatedDateAsc(UUID.randomUUID()).isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Balasan Publik Terakhir Mengabaikan Catatan Internal")
    void testFindLastPublicReply() {
        seedConversation();

        TicketReply last = replyRepo
                .findFirstByTicket_IdAndIsInternalNoteFalseOrderByCreatedDateDesc(ticket.getId()).orElseThrow();

        Assertions.assertEquals("Sedang kami cek", last.getMessage());
        Assertions.assertFalse(last.getIsInternalNote());
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Antrean Notifikasi Email")
    void testFindPendingEmail() {
        seedConversation();

        List<TicketReply> pending = replyRepo.findByIsEmailSentFalseAndIsInternalNoteFalse();

        Assertions.assertEquals(1, pending.size(), "Catatan internal tidak pernah masuk antrean email");
        Assertions.assertEquals("Sedang kami cek", pending.get(0).getMessage());
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Halaman Balasan Terbaru")
    void testFindPagedNewestFirst() {
        seedConversation();

        Assertions.assertEquals(3, replyRepo
                .findByTicket_IdOrderByCreatedDateDesc(ticket.getId(), PageRequest.of(0, 10)).getTotalElements());
        Assertions.assertEquals("Sedang kami cek", replyRepo
                .findByTicket_IdOrderByCreatedDateDesc(ticket.getId(), PageRequest.of(0, 1))
                .getContent().get(0).getMessage());
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Delete Reply")
    void testDeleteReply() {
        TicketReply reply = createReply(ticket, client, TicketAuthorType.CLIENT, "Akan dihapus", false, false);
        entityManager.flush();

        replyRepo.delete(reply);
        entityManager.flush();
        entityManager.clear();

        Assertions.assertTrue(replyRepo.findById(reply.getId()).isEmpty());
        Assertions.assertEquals(0, replyRepo.findByTicketId(ticket.getId()).size());

        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_ticket_replies WHERE uuid = ?1")
                .setParameter(1, reply.getId())
                .getSingleResult();

        Assertions.assertTrue(isDeleted instanceof Boolean ? (Boolean) isDeleted : ((Number) isDeleted).intValue() == 1,
                "Reply should be soft-deleted in the database");
    }
}
