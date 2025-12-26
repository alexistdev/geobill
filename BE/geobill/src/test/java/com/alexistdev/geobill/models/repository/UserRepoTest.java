package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

@DataJpaTest
@ActiveProfiles("test")
public class UserRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser,null,new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Test findByRole")
    void testFindByRoleNot() {
        User user1 = new User();
        user1.setFullName("Alexsander Hendra Wijaya");
        user1.setPassword("123456");
        user1.setEmail("alexistdev@gmail.com");
        user1.setRole(Role.USER);
        entityManager.persist(user1);

        User user2 = new User();
        user2.setFullName("Veronica Maya Santi");
        user2.setPassword("password");
        user2.setEmail("veronicamayasanti@gmail.com");
        user2.setRole(Role.STAFF);
        entityManager.persist(user2);

        User user3 = new User();
        user3.setFullName("John Doe");
        user3.setPassword("pass2025");
        user3.setEmail("johndoe@gmail.com");
        user3.setRole(Role.ADMIN);
        entityManager.persist(user3);

        Pageable pageable = Pageable.ofSize(10);
        Page<User> result = userRepo.findByRoleNot(Role.ADMIN, pageable);

        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertTrue(result.stream().anyMatch(user -> user.getRole().equals(Role.STAFF)));
        Assertions.assertTrue(result.stream().anyMatch(user -> user.getRole().equals(Role.USER)));
        Assertions.assertFalse(result.stream().anyMatch(user -> user.getRole().equals(Role.ADMIN)));
    }

    @Test
    @DisplayName("Test Save User")
    void testSaveUser() {
        User user = new User();
        user.setFullName("Alexsander Hendra Wijaya");
        user.setPassword("123456");
        user.setEmail("alexistdev@gmail.com");
        user.setRole(Role.USER);

        User savedUser = userRepo.save(user);
        Assertions.assertNotNull(savedUser);
        Assertions.assertEquals(user.getEmail(), savedUser.getEmail());
        Assertions.assertEquals(user.getFullName(), savedUser.getFullName());
        Assertions.assertEquals(Role.USER, savedUser.getRole());
    }

    @Test
    @DisplayName("Test Find By User Id")
    void testFindByUserId() {
        User user = new User();
        user.setFullName("Alexsander Hendra Wijaya");
        user.setPassword("123456");
        user.setEmail("alexistdev@gmail.com");
        user.setRole(Role.USER);
        user.setCreatedBy("system");
        user.setCreatedDate(new java.util.Date());
        user.setModifiedBy("system");
        user.setModifiedDate(new java.util.Date());
        user.setDeleted(false);
        entityManager.persist(user);

        Optional<User> foundUser = userRepo.findById(user.getId());

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
    @DisplayName("Test Find All Users")
    void testFindAllUsers() {
        User user1 = new User();
        user1.setFullName("Alexsander Hendra Wijaya");
        user1.setPassword("123456");
        user1.setEmail("alexistdev@gmail.com");
        user1.setRole(Role.USER);
        entityManager.persist(user1);

        User user2 = new User();
        user2.setFullName("Veronica Maya Santi");
        user2.setPassword("password");
        user2.setEmail("veronicamayasanti@gmail.com");
        user2.setRole(Role.USER);
        entityManager.persist(user2);

        List<User> allUsers = userRepo.findAll();
        Assertions.assertEquals(2,allUsers.size());
    }

    @Test
    @DisplayName("Test Find By Email")
    void testFindByEmail() {
        User user = new User();
        user.setFullName("Alexsander Hendra Wijaya");
        user.setPassword("123456");
        user.setEmail("alexistdev@gmail.com");
        user.setRole(Role.USER);
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> foundUser = userRepo.findByEmail("alexistdev@gmail.com");

        Assertions.assertTrue(foundUser.isPresent());
        Assertions.assertEquals(user.getEmail(), foundUser.get().getEmail());
        Assertions.assertEquals(user.getFullName(), foundUser.get().getFullName());
        Assertions.assertEquals(Role.USER, foundUser.get().getRole());
    }

    @Test
    @DisplayName("Test Delete User")
    void testDeleteUser() {
        User user = new User();
        user.setFullName("Alexsander Hendra Wijaya");
        user.setPassword("123456");
        user.setEmail("alexistdev@gmail.com");
        user.setRole(Role.USER);
        entityManager.persist(user);

        userRepo.delete(user);
        Optional<User> deletedUser = userRepo.findById(user.getId());
        Assertions.assertFalse(deletedUser.isPresent());
    }

    @Test
    @DisplayName("Test Find Email Not Exist")
    void testFindEmailNotExist() {
        Optional<User> foundUser = userRepo.findByEmail("nonexistent@gmail.com");
        Assertions.assertFalse(foundUser.isPresent());
    }
}
