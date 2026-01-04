package com.alexistdev.geobill.utils;

import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.RoleMenu;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.MenuRepo;
import com.alexistdev.geobill.models.repository.RoleMenuRepo;
import com.alexistdev.geobill.models.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {


    private final MenuRepo menuRepo;
    private final RoleMenuRepo roleMenuRepo;
    private final UserRepo userRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final String SYSTEM_USER = "System";
    private static final String DEFAULT_PASSWORD = "password";

    @Override
    public void run(String... args) {
        if (menuRepo.count() == 0) {
            seedMenus();
            seedRoleMenus();
            seedUsers();
        }
    }

    private void seedMenus() {
        log.info("Seeding menus");

        Menu menu1 = createMenu("Dashboard", "/admin/dashboard", "menu-title d-flex align-items-center", 1, null,1, "ad1","bx bx-home-alt");
        Menu menu2 = createMenu("Dashboard", "/users/dashboard", "menu-title d-flex align-items-center", 1, null,2,"us1","bx bx-home-alt");
        Menu menu3 = createMenu("Services", "#", "menu-title d-flex align-items-center", 2, null,2,"us2","bx bx-collection");
        menuRepo.saveAll(List.of(menu1, menu2, menu3));

        Optional<Menu> menuParent = menuRepo.findByCode("us2");

        menuParent.ifPresent(menu -> {
            Menu menuChild = createMenu("My Services", "/users/services", "", 1, menu.getId(),2,"us3","bx bx-server");
            menuRepo.save(menuChild);
        });

        log.info("Finished seeding menus");
    }

    private void seedRoleMenus() {
        log.info("Seeding role menus");

        Optional<Menu> menuAdmin = menuRepo.findByCode("ad1");
        Optional<Menu> menuUser1 = menuRepo.findByCode("us1");
        Optional<Menu> menuUser2 = menuRepo.findByCode("us2");
        Optional<Menu> menuUser3 = menuRepo.findByCode("us3");

        List<RoleMenu> roleMenus = List.of(
                Objects.requireNonNull(menuAdmin.map(menu2 -> createRoleMenu(Role.ADMIN, menu2)).orElse(null)),
                Objects.requireNonNull(menuUser1.map(menu1 -> createRoleMenu(Role.USER, menu1)).orElse(null)),
                Objects.requireNonNull(menuUser2.map(value -> createRoleMenu(Role.USER, value)).orElse(null)),
                Objects.requireNonNull(menuUser3.map(menu -> createRoleMenu(Role.USER, menu)).orElse(null)));
        roleMenuRepo.saveAll(roleMenus);

        log.info("Finished seeding role menus");
    }

    private void seedUsers() {
        log.info("Seeding users");
        User user = createUser("user", "user@gmail.com", Role.USER);
        User admin = createUser("admin", "admin@gmail.com", Role.ADMIN);
        List<User> users = List.of(user, admin);
        userRepo.saveAll(users);
        log.info("Finished seeding users");
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

    private RoleMenu createRoleMenu(Role role, Menu menu) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRole(role);
        roleMenu.setMenu(menu);
        roleMenu.setCreatedBy(SYSTEM_USER);
        roleMenu.setModifiedBy(SYSTEM_USER);
        roleMenu.setCreatedDate(new java.util.Date());
        roleMenu.setModifiedDate(new java.util.Date());
        roleMenu.setIsDeleted(false);
        return roleMenu;
    }

    private User createUser(String fullName, String email, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(bCryptPasswordEncoder.encode(DatabaseSeeder.DEFAULT_PASSWORD));
        user.setDeleted(false);
        user.setCreatedBy(SYSTEM_USER);
        user.setModifiedBy(SYSTEM_USER);
        user.setCreatedDate(new java.util.Date());
        user.setModifiedDate(new java.util.Date());
        return user;
    }
}
