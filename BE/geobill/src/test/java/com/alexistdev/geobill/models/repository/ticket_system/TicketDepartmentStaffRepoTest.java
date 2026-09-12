package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.TicketDepartment;
import com.alexistdev.geobill.models.entity.TicketDepartmentStaff;
import com.alexistdev.geobill.models.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class TicketDepartmentStaffRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketDepartmentStaffRepo departmentStaffRepo;

    private TicketDepartment technical;
    private TicketDepartment billing;
    private User staffAndi;
    private User staffBudi;

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
        staffAndi = createUser("Andi", "andi@geobill.test", Role.STAFF);
        staffBudi = createUser("Budi", "budi@geobill.test", Role.STAFF);
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

    private TicketDepartmentStaff assign(TicketDepartment department, User user, boolean supervisor) {
        TicketDepartmentStaff link = new TicketDepartmentStaff();
        link.setDepartment(department);
        link.setUser(user);
        link.setIsSupervisor(supervisor);
        return link;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save Department Staff")
    void testSaveDepartmentStaff() {
        TicketDepartmentStaff saved = departmentStaffRepo.save(assign(technical, staffAndi, true));

        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(technical.getId(), saved.getDepartment().getId());
        Assertions.assertEquals(staffAndi.getId(), saved.getUser().getId());
        Assertions.assertTrue(saved.getIsSupervisor());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Exists And Find By Department And User")
    void testExistsAndFind() {
        entityManager.persist(assign(technical, staffAndi, false));
        entityManager.flush();
        entityManager.clear();

        Assertions.assertTrue(departmentStaffRepo.existsByDepartment_IdAndUser_Id(technical.getId(), staffAndi.getId()));
        Assertions.assertFalse(departmentStaffRepo.existsByDepartment_IdAndUser_Id(billing.getId(), staffAndi.getId()));
        Assertions.assertTrue(departmentStaffRepo
                .findByDepartment_IdAndUser_Id(technical.getId(), staffAndi.getId()).isPresent());
        Assertions.assertTrue(departmentStaffRepo
                .findByDepartment_IdAndUser_Id(technical.getId(), staffBudi.getId()).isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Find Staff By Department Ordered By Name")
    void testFindStaffByDepartmentId() {
        entityManager.persist(assign(technical, staffBudi, false));
        entityManager.persist(assign(technical, staffAndi, false));
        entityManager.persist(assign(billing, staffBudi, false));
        entityManager.flush();
        entityManager.clear();

        List<User> staffs = departmentStaffRepo.findStaffByDepartmentId(technical.getId());

        Assertions.assertEquals(2, staffs.size());
        Assertions.assertEquals("Andi", staffs.get(0).getFullName());
        Assertions.assertEquals("Budi", staffs.get(1).getFullName());
        Assertions.assertEquals(1, departmentStaffRepo.findStaffByDepartmentId(billing.getId()).size());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Find Supervisors Only")
    void testFindSupervisors() {
        entityManager.persist(assign(technical, staffAndi, true));
        entityManager.persist(assign(technical, staffBudi, false));
        entityManager.flush();
        entityManager.clear();

        List<User> supervisors = departmentStaffRepo.findSupervisorsByDepartmentId(technical.getId());

        Assertions.assertEquals(1, supervisors.size());
        Assertions.assertEquals("Andi", supervisors.get(0).getFullName());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Find By Department And By User")
    void testFindByDepartmentAndByUser() {
        entityManager.persist(assign(technical, staffAndi, false));
        entityManager.persist(assign(billing, staffAndi, false));
        entityManager.flush();
        entityManager.clear();

        Assertions.assertEquals(1, departmentStaffRepo
                .findByDepartment_IdOrderByUser_FullNameAsc(technical.getId()).size());
        Assertions.assertEquals(2, departmentStaffRepo.findByUser_Id(staffAndi.getId()).size(),
                "Satu staff boleh menangani lebih dari satu departemen");
        Assertions.assertEquals(0, departmentStaffRepo.findByUser_Id(staffBudi.getId()).size());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Delete Department Staff")
    void testDeleteDepartmentStaff() {
        TicketDepartmentStaff link = assign(technical, staffAndi, false);
        entityManager.persist(link);
        entityManager.flush();

        departmentStaffRepo.delete(link);
        entityManager.flush();
        entityManager.clear();

        Assertions.assertFalse(departmentStaffRepo
                .existsByDepartment_IdAndUser_Id(technical.getId(), staffAndi.getId()));
        Assertions.assertEquals(0, departmentStaffRepo.findStaffByDepartmentId(technical.getId()).size());

        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_ticket_department_staffs WHERE uuid = ?1")
                .setParameter(1, link.getId())
                .getSingleResult();

        Assertions.assertTrue(isDeleted instanceof Boolean ? (Boolean) isDeleted : ((Number) isDeleted).intValue() == 1,
                "Department staff should be soft-deleted in the database");
    }
}
