package com.alexistdev.geobill.models.repository;


import com.alexistdev.geobill.models.entity.BaseEntity;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class UserRepoTest {

    private final TestEntityManager entityManager;
    private final UserRepo userRepo;

    @Autowired
    public UserRepoTest(TestEntityManager entityManager, UserRepo userRepo) {
        this.entityManager = entityManager;
        this.userRepo = userRepo;
    }

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User createUser(String fullName, String email, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setPassword("123456");
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test findByRole Not Admin")
    void testFindByRoleNotAdmin() {
        User user1 = createUser("user test1", "user1@gmail.com", Role.USER);
        entityManager.persist(user1);
        entityManager.flush();
        entityManager.clear();

        User user2 = createUser("user test2", "user2@gmail.com", Role.STAFF);
        entityManager.persist(user2);
        entityManager.flush();
        entityManager.clear();

        User user3 = createUser("user test3", "user3@gmail.com", Role.ADMIN);
        entityManager.persist(user3);
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = Pageable.ofSize(10);
        Page<User> result = userRepo.findByRoleNot(Role.ADMIN, pageable);

        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertTrue(result.stream().anyMatch(user -> user.getRole().equals(Role.STAFF)));
        Assertions.assertTrue(result.stream().anyMatch(user -> user.getRole().equals(Role.USER)));
        Assertions.assertFalse(result.stream().anyMatch(user -> user.getRole().equals(Role.ADMIN)));
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Save User")
    void testSaveUser() {
        User user = createUser("user test1", "user1@gmail.com", Role.USER);
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        User savedUser = userRepo.save(user);
        Assertions.assertNotNull(savedUser);
        Assertions.assertEquals(user.getEmail(), savedUser.getEmail());
        Assertions.assertEquals(user.getFullName(), savedUser.getFullName());
        Assertions.assertEquals(Role.USER, savedUser.getRole());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Find By User UUID")
    void testFindByUserUUID() {
        User user = createUser("user test1", "user1@gmail.com", Role.USER);
        User savedUser = entityManager.persistFlushFind(user);
        UUID userId = savedUser.getId();
        entityManager.clear();

        Optional<User> foundUser = userRepo.findById(userId);

        Assertions.assertTrue(foundUser.isPresent());
        Assertions.assertEquals(user.getEmail(), foundUser.get().getEmail());
        Assertions.assertEquals(user.getFullName(), foundUser.get().getFullName());
        Assertions.assertEquals(Role.USER, foundUser.get().getRole());
        Assertions.assertNotNull(foundUser.get().getCreatedDate());
        Assertions.assertNotNull(foundUser.get().getModifiedDate());
        Assertions.assertNotNull(foundUser.get().getCreatedBy());
        Assertions.assertNotNull(foundUser.get().getModifiedBy());
        Assertions.assertFalse(foundUser.get().getDeleted());
        Assertions.assertNotNull(foundUser.get().getId());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Find All Users")
    void testFindAllUsers() {
        User user1 = createUser("user test1", "user1@gmail.com", Role.USER);
        entityManager.persist(user1);
        entityManager.flush();
        entityManager.clear();

        User user2 = createUser("user test2", "user2@gmail.com", Role.STAFF);
        entityManager.persist(user2);
        entityManager.flush();
        entityManager.clear();

        User user3 = createUser("user test3", "user3@gmail.com", Role.ADMIN);
        entityManager.persist(user3);
        entityManager.flush();
        entityManager.clear();

        List<User> allUsers = userRepo.findAll();
        Assertions.assertEquals(3, allUsers.size());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Find By Email")
    void testFindByEmail() {
        User user1 = createUser("user test1", "user1@gmail.com", Role.USER);
        entityManager.persist(user1);
        entityManager.flush();
        entityManager.clear();

        Optional<User> foundUser = userRepo.findByEmail(user1.getEmail());

        Assertions.assertTrue(foundUser.isPresent());
        Assertions.assertEquals(user1.getEmail(), foundUser.get().getEmail());
        Assertions.assertEquals(user1.getFullName(), foundUser.get().getFullName());
        Assertions.assertEquals(Role.USER, foundUser.get().getRole());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Delete User")
    void testDeleteUser() {
        User user = createUser("user test1", "user1@gmail.com", Role.USER);
        User savedUser = entityManager.persist(user);
        entityManager.flush();

        userRepo.delete(savedUser);
        entityManager.flush();
        entityManager.clear();

        // Standard findById should NOT find the user because of
        // @Where(clause="is_deleted=false")
        Optional<User> checkStandardFind = userRepo.findById(savedUser.getId());
        Assertions.assertFalse(checkStandardFind.isPresent(),
                "User should not be found via standard repository methods due to soft delete");

        // Native query to check DB state directly
        Object result = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_users WHERE id = ?1")
                .setParameter(1, savedUser.getId())
                .getSingleResult();

        Assertions.assertNotNull(result);
        boolean isDeleted = false;
        if (result instanceof Boolean) {
            isDeleted = (Boolean) result;
        } else if (result instanceof Number) {
            isDeleted = ((Number) result).intValue() == 1;
        }
        Assertions.assertTrue(isDeleted, "User should be marked as deleted in the database");
    }

    @Test
    @Order(7)
    @DisplayName("7. Test findByFilter")
    void testFindByFilter() {
        User user1 = new User();
        user1.setFullName("Alexsander");
        user1.setEmail("alex@gmail.com");
        user1.setRole(Role.USER);
        user1.setDeleted(false);
        entityManager.persist(user1);

        User user2 = new User();
        user2.setFullName("Staff Member");
        user2.setEmail("staff@gmail.com");
        user2.setRole(Role.STAFF);
        user2.setDeleted(false);
        entityManager.persist(user2);

        User admin = new User();
        admin.setFullName("Admin User");
        admin.setEmail("admin@gmail.com");
        admin.setRole(Role.ADMIN);
        admin.setDeleted(false);
        entityManager.persist(admin);

        User deletedUser = new User();
        deletedUser.setFullName("Deleted User");
        deletedUser.setEmail("deleted@gmail.com");
        deletedUser.setRole(Role.USER);
        deletedUser.setDeleted(true);
        entityManager.persist(deletedUser);

        Pageable pageable = Pageable.ofSize(10);

        // Test keyword filter - searching for "alex" should find user1
        Page<User> result = userRepo.findByFilter("alex", pageable);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Alexsander", result.getContent().getFirst().getFullName());

        // Test exclusion of ADMIN and Deleted
        // admin matches "admin@gmail.com" but has ADMIN role, so it should be excluded.
        // deletedUser matches "deleted@gmail.com" but isDeleted=true and @Where filters it out.
        result = userRepo.findByFilter("", pageable);
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertTrue(result.stream().noneMatch(u -> u.getRole() == Role.ADMIN));
        Assertions.assertTrue(result.stream().noneMatch(BaseEntity::getDeleted));
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Find Email Not Exist")
    void testFindEmailNotExist() {
        Optional<User> foundUser = userRepo.findByEmail("nonexistent@gmail.com");
        Assertions.assertFalse(foundUser.isPresent());
    }
}
