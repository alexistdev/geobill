package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.TicketDepartment;
import com.alexistdev.geobill.models.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class TicketDepartmentRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketDepartmentRepo departmentRepo;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private TicketDepartment createDepartment(String name, String code, int sortOrder, boolean active) {
        TicketDepartment department = new TicketDepartment();
        department.setName(name);
        department.setCode(code);
        department.setSortOrder(sortOrder);
        department.setIsActive(active);
        return department;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save Department")
    void testSaveDepartment() {
        TicketDepartment department = createDepartment("Technical Support", "TECH", 1, true);
        department.setEmail("support@geobill.test");
        department.setAutoCloseDays(7);

        TicketDepartment saved = departmentRepo.save(department);

        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("Technical Support", saved.getName());
        Assertions.assertEquals("TECH", saved.getCode());
        Assertions.assertEquals(7, saved.getAutoCloseDays());
        Assertions.assertTrue(saved.getIsActive());
        Assertions.assertFalse(saved.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Find By Code")
    void testFindByCode() {
        entityManager.persist(createDepartment("Billing", "BILLING", 2, true));
        entityManager.flush();

        Optional<TicketDepartment> found = departmentRepo.findByCode("BILLING");

        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Billing", found.get().getName());
        Assertions.assertTrue(departmentRepo.findByCode("UNKNOWN").isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Exists By Code And Name")
    void testExists() {
        entityManager.persist(createDepartment("Sales", "SALES", 3, true));
        entityManager.flush();

        Assertions.assertTrue(departmentRepo.existsByCode("SALES"));
        Assertions.assertTrue(departmentRepo.existsByName("Sales"));
        Assertions.assertFalse(departmentRepo.existsByCode("MARKETING"));
        Assertions.assertFalse(departmentRepo.existsByName("Marketing"));
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Find Active Ordered By Sort Order")
    void testFindActiveOrdered() {
        entityManager.persist(createDepartment("General", "GENERAL", 3, true));
        entityManager.persist(createDepartment("Technical Support", "TECH", 1, true));
        entityManager.persist(createDepartment("Billing", "BILLING", 2, true));
        entityManager.persist(createDepartment("Legacy", "LEGACY", 4, false));
        entityManager.flush();
        entityManager.clear();

        List<TicketDepartment> active = departmentRepo.findByIsActiveTrueOrderBySortOrderAsc();

        Assertions.assertEquals(3, active.size(), "Departemen non-aktif tidak boleh ikut");
        Assertions.assertEquals("TECH", active.get(0).getCode());
        Assertions.assertEquals("BILLING", active.get(1).getCode());
        Assertions.assertEquals("GENERAL", active.get(2).getCode());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Find All Not Deleted")
    void testFindByIsDeletedFalse() {
        entityManager.persist(createDepartment("Technical Support", "TECH", 1, true));
        entityManager.persist(createDepartment("Legacy", "LEGACY", 2, false));
        entityManager.flush();
        entityManager.clear();

        Page<TicketDepartment> page = departmentRepo.findByIsDeletedFalse(PageRequest.of(0, 10));

        Assertions.assertEquals(2, page.getTotalElements(),
                "Departemen non-aktif tetap terbaca admin, hanya tidak bisa dipilih klien");
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Find By Filter")
    void testFindByFilter() {
        entityManager.persist(createDepartment("Technical Support", "TECH", 1, true));
        entityManager.persist(createDepartment("Billing", "BILLING", 2, true));
        entityManager.flush();
        entityManager.clear();

        Page<TicketDepartment> result = departmentRepo.findByFilter("tech", PageRequest.of(0, 10));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("TECH", result.getContent().get(0).getCode());
        Assertions.assertEquals(0, departmentRepo.findByFilter("marketing", PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Delete Department")
    void testDeleteDepartment() {
        TicketDepartment department = createDepartment("Legacy", "LEGACY", 9, true);
        entityManager.persist(department);
        entityManager.flush();

        departmentRepo.delete(department);
        entityManager.flush();
        entityManager.clear();

        Assertions.assertTrue(departmentRepo.findById(department.getId()).isEmpty());
        Assertions.assertTrue(departmentRepo.findByCode("LEGACY").isEmpty());

        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_ticket_departments WHERE uuid = ?1")
                .setParameter(1, department.getId())
                .getSingleResult();

        Assertions.assertTrue(isDeleted instanceof Boolean ? (Boolean) isDeleted : ((Number) isDeleted).intValue() == 1,
                "Department should be soft-deleted in the database");
    }
}
