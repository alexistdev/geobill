package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Menu;
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
import java.util.UUID;

@DataJpaTest
@ActiveProfiles("test")
public class MenuRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MenuRepo menuRepo;

    private static final String SYSTEM_USER = "System";

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser,null,new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Menu createMenu(String name, String url, String classLink, int sortOrder, UUID parentId, int typeMenu, String code, String icon) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setUrlink(url);
        menu.setClasslink(classLink);
        menu.setIsDeleted(false);
        menu.setCreatedBy(SYSTEM_USER);
        menu.setModifiedBy(SYSTEM_USER);
        menu.setParentId(parentId);
        menu.setCreatedDate(new java.util.Date());
        menu.setModifiedDate(new java.util.Date());
        menu.setSortOrder(sortOrder);
        menu.setTypeMenu(typeMenu);
        menu.setIcon(icon);
        menu.setCode(code);
        return menu;
    }

    @Test
    @DisplayName("Test Save Menu")
    void testSaveMenu() {
        Menu menu = createMenu("Dashboard", "/admin/dashboard", "menu-title d-flex align-items-center", 1, null,1, "ad1","bx bx-home-alt");
        Menu savedMenu = menuRepo.save(menu);
        Assertions.assertNotNull(savedMenu);
        Assertions.assertEquals(menu.getName(), savedMenu.getName());
        Assertions.assertEquals(menu.getUrlink(), savedMenu.getUrlink());
        Assertions.assertEquals(menu.getClasslink(), savedMenu.getClasslink());
        Assertions.assertEquals(menu.getCreatedBy(), savedMenu.getCreatedBy());
        Assertions.assertNotNull(savedMenu.getCreatedDate());
        Assertions.assertEquals(menu.getModifiedBy(), savedMenu.getModifiedBy());
        Assertions.assertNotNull(savedMenu.getModifiedDate());
        Assertions.assertFalse(savedMenu.getIsDeleted());
        Assertions.assertEquals(menu.getSortOrder(), savedMenu.getSortOrder());
    }

    @Test
    @DisplayName("Test Find By UUID")
    void testFindByUUID(){
        Menu menu = createMenu("Dashboard", "/admin/dashboard", "menu-title d-flex align-items-center", 1, null,1, "ad1","bx bx-home-alt");
        entityManager.persist(menu);
        entityManager.flush();
        Optional<Menu> foundMenu = menuRepo.findById(menu.getId());
        Assertions.assertTrue(foundMenu.isPresent());
        Assertions.assertEquals(menu.getName(), foundMenu.get().getName());
    }

    @Test
    @DisplayName("Test Find All Menu")
    void testFindAllMenu() {
        Menu menu1 = createMenu("Dashboard", "/admin/dashboard", "menu-title d-flex align-items-center", 1, null,1, "ad1","bx bx-home-alt");
        entityManager.persist(menu1);
        entityManager.flush();

        Menu menu2 = createMenu("Dashboard", "/admin/dashboard", "menu-title d-flex align-items-center", 1, null,1, "ad1","bx bx-home-alt");
        entityManager.persist(menu2);
        entityManager.flush();

        List<Menu> allMenus = menuRepo.findAll();
        Assertions.assertEquals(2, allMenus.size());
    }

    @Test
    @DisplayName("Test Delete Menu")
    void testDeleteMenu(){
        Menu menu = createMenu("Dashboard", "/admin/dashboard", "menu-title d-flex align-items-center", 1, null,1, "ad1","bx bx-home-alt");
        entityManager.persist(menu);
        entityManager.flush();
        menuRepo.delete(menu);
        Optional<Menu> deletedMenu = menuRepo.findById(menu.getId());
        Assertions.assertFalse(deletedMenu.isPresent());
    }

    @Test
    @DisplayName("Test Delete All Menus")
    void testDeleteAllMenus() {
        Menu menu1 = createMenu("Dashboard", "/admin/dashboard", "menu-title d-flex align-items-center", 1, null,1, "ad1","bx bx-home-alt");
        entityManager.persist(menu1);
        entityManager.flush();

        Menu menu2 = createMenu("Dashboard", "/users/dashboard", "menu-title d-flex align-items-center", 1, null,2,"us1","bx bx-home-alt");
        entityManager.persist(menu2);
        entityManager.flush();

        menuRepo.deleteAll();
        List<Menu> allMenus = menuRepo.findAll();
        Assertions.assertEquals(0, allMenus.size());
    }

    @Test
    @DisplayName("Test Update Menus")
    void testUpdateMenus() {
        Menu menu = createMenu("Dashboard", "/admin/dashboard", "menu-title d-flex align-items-center", 1, null,1, "ad1","bx bx-home-alt");
        entityManager.persist(menu);
        entityManager.flush();

        menu.setName("Dashboard Updated");
        menuRepo.save(menu);
        entityManager.flush();

        Optional<Menu> updatedMenu = menuRepo.findById(menu.getId());
        Assertions.assertTrue(updatedMenu.isPresent());
        Assertions.assertEquals("Dashboard Updated", updatedMenu.get().getName());
    }

    @Test
    @DisplayName("Test Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(Serializable.class,menuRepo);
    }

    @Test
    @DisplayName("Test Menu Entity")
    void testMenuEntity() {
        Assertions.assertInstanceOf(Menu.class,new Menu());
    }

    @Test
    @DisplayName("Test Menu Repo")
    void testMenuRepo() {
        Assertions.assertInstanceOf(MenuRepo.class,menuRepo);
    }
}
