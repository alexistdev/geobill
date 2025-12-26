package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.RoleMenu;
import com.alexistdev.geobill.models.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
public class RoleMenuRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleMenuRepo roleMenuRepo;

    @BeforeEach
    void setUp() {


        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser,null,new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Menu createMenu(String name, String urlink, String classlink) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setUrlink(urlink);
        menu.setClasslink(classlink);
        menu.setCreatedBy("system");
        menu.setCreatedDate(new java.util.Date());
        menu.setModifiedBy("system");
        menu.setModifiedDate(new java.util.Date());
        menu.setIsDeleted(false);
        menu.setSortOrder(1);
        return menu;
    }

    private RoleMenu createRoleMenu(Role role, Menu menu) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRole(role);
        roleMenu.setMenu(menu);
        roleMenu.setCreatedBy("system");
        roleMenu.setCreatedDate(new java.util.Date());
        roleMenu.setModifiedBy("system");
        roleMenu.setModifiedDate(new java.util.Date());
        roleMenu.setIsDeleted(false);
        return roleMenu;
    }

    @Test
    @DisplayName("Test Save RoleMenu")
    void testSaveRoleMenu() {
        Menu menu = createMenu("Dashboard","/dashboard","DashboardController");
        entityManager.persist(menu);
        entityManager.flush();

        RoleMenu roleMenu = createRoleMenu(Role.ADMIN,menu);
        entityManager.persist(roleMenu);
        entityManager.flush();

        RoleMenu savedRoleMenu = roleMenuRepo.save(roleMenu);

        Assertions.assertNotNull(savedRoleMenu);
        Assertions.assertEquals(roleMenu.getRole(),savedRoleMenu.getRole());
        Assertions.assertEquals(roleMenu.getMenu(),savedRoleMenu.getMenu());
        Assertions.assertEquals(roleMenu.getCreatedBy(),savedRoleMenu.getCreatedBy());
        Assertions.assertEquals(roleMenu.getCreatedDate(),savedRoleMenu.getCreatedDate());
        Assertions.assertEquals(roleMenu.getModifiedBy(),savedRoleMenu.getModifiedBy());
        Assertions.assertEquals(roleMenu.getModifiedDate(),savedRoleMenu.getModifiedDate());
        Assertions.assertFalse(savedRoleMenu.getIsDeleted());
    }

    @Test
    @DisplayName("Test Find By RoleMenu UUID")
    void testFindByRoleMenuUUID(){
        Menu menu = createMenu("Dashboard","/dashboard","DashboardController");
        entityManager.persist(menu);
        entityManager.flush();

        RoleMenu roleMenu = createRoleMenu(Role.ADMIN,menu);
        entityManager.persist(roleMenu);
        entityManager.flush();

        Optional<RoleMenu> foundRoleMenu = roleMenuRepo.findById(roleMenu.getId());
        Assertions.assertTrue(foundRoleMenu.isPresent());
        Assertions.assertEquals(roleMenu.getRole(),foundRoleMenu.get().getRole());
        Assertions.assertEquals(roleMenu.getMenu(),foundRoleMenu.get().getMenu());
    }

    @Test
    @DisplayName("Test Find All RoleMenu")
    void testFindAllRoleMenu() {
        Menu menu = createMenu("Dashboard","/dashboard","DashboardController");
        entityManager.persist(menu);
        entityManager.flush();

        RoleMenu roleMenu1 = createRoleMenu(Role.ADMIN,menu);
        entityManager.persist(roleMenu1);
        entityManager.flush();

        RoleMenu roleMenu2 = createRoleMenu(Role.USER,menu);
        entityManager.persist(roleMenu2);
        entityManager.flush();

        RoleMenu roleMenu3 = createRoleMenu(Role.STAFF,menu);
        entityManager.persist(roleMenu3);
        entityManager.flush();

        List<RoleMenu> allRoleMenu = roleMenuRepo.findAll();
        Assertions.assertEquals(3,allRoleMenu.size());
    }

    @Test
    @DisplayName("Test Delete RoleMenu")
    void testDeleteRoleMenu() {
        Menu menu = createMenu("Dashboard","/dashboard","DashboardController");
        entityManager.persist(menu);
        entityManager.flush();

        RoleMenu roleMenu = createRoleMenu(Role.ADMIN,menu);
        entityManager.persist(roleMenu);
        entityManager.flush();

        roleMenuRepo.delete(roleMenu);
        Optional<RoleMenu> deletedRoleMenu = roleMenuRepo.findById(roleMenu.getId());
        Assertions.assertFalse(deletedRoleMenu.isPresent());
    }

    @Test
    @DisplayName("Test Delete All RoleMenu")
    void testDeleteAllRoleMenu() {
        Menu menu1 = createMenu("Dashboard","/dashboard","DashboardController");
        entityManager.persist(menu1);
        entityManager.flush();

        Menu menu2 = createMenu("Settings","/settings","SettingsController");
        entityManager.persist(menu2);
        entityManager.flush();

        RoleMenu roleMenu1 = createRoleMenu(Role.ADMIN,menu1);
        entityManager.persist(roleMenu1);
        entityManager.flush();

        RoleMenu roleMenu2 = createRoleMenu(Role.USER,menu2);
        entityManager.persist(roleMenu2);
        entityManager.flush();

        roleMenuRepo.deleteAll();
        List<RoleMenu> allRoleMenu = roleMenuRepo.findAll();
        Assertions.assertEquals(0,allRoleMenu.size());
    }

    @Test
    void testUpdateRoleMenu() {
        Menu menu = createMenu("Dashboard","/dashboard","DashboardController");
        entityManager.persist(menu);
        entityManager.flush();

        RoleMenu roleMenu = createRoleMenu(Role.ADMIN,menu);
        entityManager.persist(roleMenu);

        roleMenu.setRole(Role.USER);
        roleMenuRepo.save(roleMenu);

        Optional<RoleMenu> updatedRoleMenu = roleMenuRepo.findById(roleMenu.getId());
        Assertions.assertTrue(updatedRoleMenu.isPresent());
        Assertions.assertEquals(Role.USER,updatedRoleMenu.get().getRole());
    }

    @Test
    @DisplayName("Test Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(Serializable.class,roleMenuRepo);
    }

    @Test
    @DisplayName("Test RoleMenu Entity")
    void testRoleMenuEntity() {
        Assertions.assertInstanceOf(RoleMenu.class,new RoleMenu());
    }

    @Test
    @DisplayName("Test RoleMenu Repo")
    void testMenuEntity() {
        Assertions.assertInstanceOf(RoleMenuRepo.class,roleMenuRepo);
    }
}
