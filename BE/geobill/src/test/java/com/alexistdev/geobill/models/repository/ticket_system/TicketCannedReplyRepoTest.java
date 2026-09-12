package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.TicketCannedReply;
import com.alexistdev.geobill.models.entity.TicketDepartment;
import com.alexistdev.geobill.models.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class TicketCannedReplyRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketCannedReplyRepo cannedReplyRepo;

    private TicketDepartment technical;
    private TicketDepartment billing;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        technical = createDepartment("Technical Support", "TECH");
        billing = createDepartment("Billing", "BILLING");
        entityManager.flush();
    }

    private TicketDepartment createDepartment(String name, String code) {
        TicketDepartment department = new TicketDepartment();
        department.setName(name);
        department.setCode(code);
        return entityManager.persist(department);
    }

    private TicketCannedReply createCannedReply(TicketDepartment department, String title, int sortOrder,
                                                boolean active) {
        TicketCannedReply cannedReply = new TicketCannedReply();
        cannedReply.setDepartment(department);
        cannedReply.setTitle(title);
        cannedReply.setBody("Isi template " + title);
        cannedReply.setSortOrder(sortOrder);
        cannedReply.setIsActive(active);
        return cannedReply;
    }

    private void seedCannedReplies() {
        entityManager.persist(createCannedReply(null, "Sapaan global", 1, true));
        entityManager.persist(createCannedReply(technical, "Minta akses cPanel", 2, true));
        entityManager.persist(createCannedReply(billing, "Konfirmasi pembayaran", 3, true));
        entityManager.persist(createCannedReply(technical, "Template lama", 4, false));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save Canned Reply")
    void testSaveCannedReply() {
        TicketCannedReply saved = cannedReplyRepo.save(createCannedReply(technical, "Minta akses cPanel", 1, true));

        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(technical.getId(), saved.getDepartment().getId());
        Assertions.assertEquals("Minta akses cPanel", saved.getTitle());
        Assertions.assertTrue(saved.getIsActive());
        Assertions.assertFalse(saved.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Template Aktif Urut Sort Order")
    void testFindActiveOrdered() {
        seedCannedReplies();

        List<TicketCannedReply> active = cannedReplyRepo.findByIsActiveTrueOrderBySortOrderAsc();

        Assertions.assertEquals(3, active.size(), "Template non-aktif tidak boleh ikut");
        Assertions.assertEquals("Sapaan global", active.get(0).getTitle());
        Assertions.assertEquals("Minta akses cPanel", active.get(1).getTitle());
        Assertions.assertEquals("Konfirmasi pembayaran", active.get(2).getTitle());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Exists By Title")
    void testExistsByTitle() {
        seedCannedReplies();

        Assertions.assertTrue(cannedReplyRepo.existsByTitle("Sapaan global"));
        Assertions.assertFalse(cannedReplyRepo.existsByTitle("Tidak ada"));
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Template Departemen Digabung Dengan Template Global")
    void testFindAvailableForDepartment() {
        seedCannedReplies();

        List<TicketCannedReply> forTechnical = cannedReplyRepo.findAvailableForDepartment(technical.getId());

        Assertions.assertEquals(2, forTechnical.size(),
                "Template milik departemen ditambah template global");
        Assertions.assertEquals("Sapaan global", forTechnical.get(0).getTitle());
        Assertions.assertEquals("Minta akses cPanel", forTechnical.get(1).getTitle());
        Assertions.assertTrue(forTechnical.stream().noneMatch(c -> "Template lama".equals(c.getTitle())),
                "Template non-aktif tidak boleh muncul");

        List<TicketCannedReply> forBilling = cannedReplyRepo.findAvailableForDepartment(billing.getId());
        Assertions.assertEquals(2, forBilling.size());
        Assertions.assertTrue(forBilling.stream().anyMatch(c -> "Konfirmasi pembayaran".equals(c.getTitle())));
        Assertions.assertTrue(forBilling.stream().noneMatch(c -> "Minta akses cPanel".equals(c.getTitle())),
                "Template departemen lain tidak boleh bocor");
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Halaman Semua Template")
    void testFindByIsDeletedFalse() {
        seedCannedReplies();

        Assertions.assertEquals(4, cannedReplyRepo.findByIsDeletedFalse(PageRequest.of(0, 10)).getTotalElements(),
                "Admin tetap melihat template non-aktif");
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Delete Canned Reply")
    void testDeleteCannedReply() {
        TicketCannedReply cannedReply = createCannedReply(technical, "Akan dihapus", 5, true);
        entityManager.persist(cannedReply);
        entityManager.flush();

        cannedReplyRepo.delete(cannedReply);
        entityManager.flush();
        entityManager.clear();

        Assertions.assertFalse(cannedReplyRepo.existsByTitle("Akan dihapus"));
        Assertions.assertEquals(0, cannedReplyRepo.findAvailableForDepartment(technical.getId()).size());

        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_ticket_canned_replies WHERE uuid = ?1")
                .setParameter(1, cannedReply.getId())
                .getSingleResult();

        Assertions.assertTrue(isDeleted instanceof Boolean ? (Boolean) isDeleted : ((Number) isDeleted).intValue() == 1,
                "Canned reply should be soft-deleted in the database");
    }
}
