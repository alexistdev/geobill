package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class TicketRepoTest {

    private static final long ONE_DAY = 24L * 60 * 60 * 1000;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketRepo ticketRepo;

    private TicketDepartment technical;
    private TicketDepartment billing;
    private User client;
    private User otherClient;
    private User staff;
    private Date now;
    private Date lastWeek;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        now = new Date();
        lastWeek = new Date(now.getTime() - 7 * ONE_DAY);

        technical = createDepartment("Technical Support", "TECH");
        billing = createDepartment("Billing", "BILLING");
        client = createUser("Client One", "client1@geobill.test", Role.USER);
        otherClient = createUser("Client Two", "client2@geobill.test", Role.USER);
        staff = createUser("Staff One", "staff1@geobill.test", Role.STAFF);
        entityManager.flush();
    }

    private TicketDepartment createDepartment(String name, String code) {
        TicketDepartment department = new TicketDepartment();
        department.setName(name);
        department.setCode(code);
        return entityManager.persist(department);
    }

    private User createUser(String fullName, String email, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        return entityManager.persist(user);
    }

    private Ticket createTicket(String number, String subject, User owner, TicketDepartment department,
                                TicketStatus status, TicketPriority priority, User assignee, Date lastReplyAt) {
        Ticket ticket = new Ticket();
        ticket.setTicketNumber(number);
        ticket.setSubject(subject);
        ticket.setUser(owner);
        ticket.setDepartment(department);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setAssignedTo(assignee);
        ticket.setLastReplyAt(lastReplyAt);
        return ticket;
    }

    /** Empat tiket yang mewakili kombinasi status, prioritas, departemen, dan assignee. */
    private void seedTickets() {
        entityManager.persist(createTicket("TKT-202609-000001", "Website down", client, technical,
                TicketStatus.IN_PROGRESS, TicketPriority.HIGH, staff, now));
        entityManager.persist(createTicket("TKT-202609-000002", "Email tidak masuk", client, technical,
                TicketStatus.IN_PROGRESS, TicketPriority.LOW, null, lastWeek));
        entityManager.persist(createTicket("TKT-202609-000003", "Tagihan ganda", client, billing,
                TicketStatus.AWAITING_CLIENT, TicketPriority.MEDIUM, staff, lastWeek));
        entityManager.persist(createTicket("TKT-202609-000004", "Salah pesan", otherClient, billing,
                TicketStatus.TRASH, TicketPriority.LOW, null, now));
        entityManager.flush();
        entityManager.clear();
    }

    private Pageable firstPage() {
        return PageRequest.of(0, 10);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save Ticket")
    void testSaveTicket() {
        Ticket ticket = createTicket("TKT-202609-000001", "Website down", client, technical,
                TicketStatus.AWAITING_STAFF, TicketPriority.HIGH, null, null);

        Ticket saved = ticketRepo.save(ticket);

        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("TKT-202609-000001", saved.getTicketNumber());
        Assertions.assertEquals(client.getId(), saved.getUser().getId());
        Assertions.assertEquals(technical.getId(), saved.getDepartment().getId());
        Assertions.assertEquals(TicketStatus.AWAITING_STAFF, saved.getStatus());
        Assertions.assertEquals(0, saved.getReplyCount());
        Assertions.assertNull(saved.getAssignedTo());
        Assertions.assertFalse(saved.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Find And Exists By Ticket Number")
    void testFindByTicketNumber() {
        seedTickets();

        Optional<Ticket> found = ticketRepo.findByTicketNumber("TKT-202609-000002");

        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Email tidak masuk", found.get().getSubject());
        Assertions.assertTrue(ticketRepo.existsByTicketNumber("TKT-202609-000001"));
        Assertions.assertFalse(ticketRepo.existsByTicketNumber("TKT-202609-999999"));
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Find By Id And User")
    void testFindByIdAndUser() {
        seedTickets();
        Ticket ticket = ticketRepo.findByTicketNumber("TKT-202609-000001").orElseThrow();

        Assertions.assertTrue(ticketRepo.findByIdAndUser(ticket.getId(), client).isPresent());
        Assertions.assertTrue(ticketRepo.findByIdAndUser(ticket.getId(), otherClient).isEmpty(),
                "Klien lain tidak boleh menemukan tiket yang bukan miliknya");
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Count By Status And Assignee")
    void testCountByStatus() {
        seedTickets();

        Assertions.assertEquals(2, ticketRepo.countByStatus(TicketStatus.IN_PROGRESS));
        Assertions.assertEquals(1, ticketRepo.countByStatus(TicketStatus.TRASH));
        Assertions.assertEquals(0, ticketRepo.countByStatus(TicketStatus.CLOSED));
        Assertions.assertEquals(1, ticketRepo.countByAssignedTo_IdAndStatus(staff.getId(), TicketStatus.IN_PROGRESS));
        Assertions.assertEquals(0, ticketRepo.countByAssignedTo_IdAndStatus(staff.getId(), TicketStatus.ON_HOLD));
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Count Group By Status Untuk Angka Tab")
    void testCountGroupByStatus() {
        seedTickets();

        Map<TicketStatus, Long> counts = ticketRepo.countGroupByStatus().stream()
                .collect(Collectors.toMap(TicketStatusCount::getStatus, TicketStatusCount::getTotal));

        Assertions.assertEquals(2L, counts.get(TicketStatus.IN_PROGRESS));
        Assertions.assertEquals(1L, counts.get(TicketStatus.AWAITING_CLIENT));
        Assertions.assertEquals(1L, counts.get(TicketStatus.TRASH), "Tiket di Trash tetap terhitung");
        Assertions.assertNull(counts.get(TicketStatus.CLOSED),
                "Status tanpa tiket tidak terbawa, pemanggil harus memakai nol sebagai default");
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Count Group By Status Milik Satu Klien")
    void testCountGroupByStatusForUser() {
        seedTickets();

        Map<TicketStatus, Long> counts = ticketRepo.countGroupByStatusForUser(client).stream()
                .collect(Collectors.toMap(TicketStatusCount::getStatus, TicketStatusCount::getTotal));

        Assertions.assertEquals(2L, counts.get(TicketStatus.IN_PROGRESS));
        Assertions.assertEquals(1L, counts.get(TicketStatus.AWAITING_CLIENT));
        Assertions.assertNull(counts.get(TicketStatus.TRASH), "Tiket milik klien lain tidak boleh ikut terhitung");
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Filter Tanpa Kriteria Hanya Menyaring Status")
    void testFindByFilterWithoutCriteria() {
        seedTickets();

        Page<Ticket> result = ticketRepo.findByFilter(TicketStatus.IN_PROGRESS, null, null, null, null, null, null,
                firstPage());

        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertTrue(result.stream().allMatch(t -> t.getStatus() == TicketStatus.IN_PROGRESS));
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Filter Nomor Tiket Dan Summary")
    void testFindByFilterNumberAndSubject() {
        seedTickets();

        Assertions.assertEquals(1, ticketRepo
                .findByFilter(TicketStatus.IN_PROGRESS, "000001", null, null, null, null, null, firstPage())
                .getTotalElements());
        Assertions.assertEquals(1, ticketRepo
                .findByFilter(TicketStatus.IN_PROGRESS, null, "email", null, null, null, null, firstPage())
                .getTotalElements(), "Pencarian summary harus mengabaikan besar kecil huruf");
        Assertions.assertEquals(0, ticketRepo
                .findByFilter(TicketStatus.IN_PROGRESS, null, "tagihan", null, null, null, null, firstPage())
                .getTotalElements(), "Tiket di tab lain tidak boleh ikut");
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Filter Prioritas, Departemen, Dan Assignee")
    void testFindByFilterPriorityDepartmentAssignee() {
        seedTickets();

        Assertions.assertEquals(1, ticketRepo
                .findByFilter(TicketStatus.IN_PROGRESS, null, null, TicketPriority.HIGH, null, null, null, firstPage())
                .getTotalElements());
        Assertions.assertEquals(2, ticketRepo
                .findByFilter(TicketStatus.IN_PROGRESS, null, null, null, technical.getId(), null, null, firstPage())
                .getTotalElements());
        Assertions.assertEquals(0, ticketRepo
                .findByFilter(TicketStatus.IN_PROGRESS, null, null, null, billing.getId(), null, null, firstPage())
                .getTotalElements());
        Assertions.assertEquals(1, ticketRepo
                .findByFilter(TicketStatus.IN_PROGRESS, null, null, null, null, staff.getId(), null, firstPage())
                .getTotalElements());
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Filter Last Reply Dan Gabungan Semua Kriteria")
    void testFindByFilterLastReplyAndCombined() {
        seedTickets();
        Date yesterday = new Date(now.getTime() - ONE_DAY);

        Assertions.assertEquals(1, ticketRepo
                .findByFilter(TicketStatus.IN_PROGRESS, null, null, null, null, null, yesterday, firstPage())
                .getTotalElements(), "Hanya tiket yang dibalas setelah batas waktu");

        Page<Ticket> combined = ticketRepo.findByFilter(TicketStatus.IN_PROGRESS, "000001", "website",
                TicketPriority.HIGH, technical.getId(), staff.getId(), yesterday, firstPage());

        Assertions.assertEquals(1, combined.getTotalElements());
        Assertions.assertEquals("TKT-202609-000001", combined.getContent().get(0).getTicketNumber());
    }

    @Test
    @Order(11)
    @DisplayName("11. Test Find By User Dan By User And Status")
    void testFindByUser() {
        seedTickets();

        Assertions.assertEquals(3, ticketRepo.findByUser(client, firstPage()).getTotalElements());
        Assertions.assertEquals(1, ticketRepo.findByUser(otherClient, firstPage()).getTotalElements());
        Assertions.assertEquals(2, ticketRepo
                .findByUserAndStatus(client, TicketStatus.IN_PROGRESS, firstPage()).getTotalElements());
        Assertions.assertEquals(0, ticketRepo
                .findByUserAndStatus(otherClient, TicketStatus.IN_PROGRESS, firstPage()).getTotalElements());
    }

    @Test
    @Order(12)
    @DisplayName("12. Test Antrean Staff Urut Dari Yang Paling Lama Diam")
    void testFindOpenByAssignedTo() {
        seedTickets();
        entityManager.persist(createTicket("TKT-202609-000005", "Sudah selesai", client, technical,
                TicketStatus.CLOSED, TicketPriority.LOW, staff, now));
        entityManager.flush();
        entityManager.clear();

        Page<Ticket> queue = ticketRepo.findOpenByAssignedTo(staff.getId(), firstPage());

        Assertions.assertEquals(2, queue.getTotalElements(), "Tiket CLOSED dan TRASH tidak masuk antrean");
        List<String> numbers = queue.stream().map(Ticket::getTicketNumber).toList();
        Assertions.assertEquals("TKT-202609-000003", numbers.get(0), "Yang paling lama tidak dibalas harus di atas");
        Assertions.assertEquals("TKT-202609-000001", numbers.get(1));
    }

    @Test
    @Order(13)
    @DisplayName("13. Test Tiket Menunggu Klien Yang Sudah Lama Diam")
    void testFindIdleAwaitingClient() {
        seedTickets();
        Date threeDaysAgo = new Date(now.getTime() - 3 * ONE_DAY);

        List<Ticket> idle = ticketRepo.findIdleAwaitingClient(threeDaysAgo);

        Assertions.assertEquals(1, idle.size());
        Assertions.assertEquals("TKT-202609-000003", idle.get(0).getTicketNumber());
        Assertions.assertTrue(ticketRepo.findIdleAwaitingClient(new Date(now.getTime() - 30 * ONE_DAY)).isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("14. Test Delete Ticket")
    void testDeleteTicket() {
        seedTickets();
        Ticket ticket = ticketRepo.findByTicketNumber("TKT-202609-000004").orElseThrow();

        ticketRepo.delete(ticket);
        entityManager.flush();
        entityManager.clear();

        Assertions.assertTrue(ticketRepo.findByTicketNumber("TKT-202609-000004").isEmpty());
        Assertions.assertEquals(0, ticketRepo.countByStatus(TicketStatus.TRASH),
                "Hapus permanen dari Trash membuat tiket hilang dari semua query");

        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_tickets WHERE uuid = ?1")
                .setParameter(1, ticket.getId())
                .getSingleResult();

        Assertions.assertTrue(isDeleted instanceof Boolean ? (Boolean) isDeleted : ((Number) isDeleted).intValue() == 1,
                "Ticket should be soft-deleted in the database");
    }

    @Test
    @Order(15)
    @DisplayName("15. Test Tiket Di Trash Masih Terbaca Query")
    void testTrashedTicketStillVisible() {
        seedTickets();

        Page<Ticket> trash = ticketRepo.findByFilter(TicketStatus.TRASH, null, null, null, null, null, null,
                firstPage());

        Assertions.assertEquals(1, trash.getTotalElements(),
                "Tab Trash memakai status, bukan soft delete, supaya tiket masih bisa di-restore");
        Assertions.assertFalse(trash.getContent().get(0).getDeleted());
    }
}
