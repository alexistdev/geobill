package com.alexistdev.geobill.utils;

import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.RoleMenu;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.MenuRepo;
import com.alexistdev.geobill.models.repository.RoleMenuRepo;
import com.alexistdev.geobill.models.repository.UserRepo;
import com.alexistdev.geobill.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private MenuRepo menuRepo;

    @Autowired
    private RoleMenuRepo roleMenuRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (menuRepo.count() == 0) { // check if the menu table is empty (not seeded)
            seedMenus();
            seedRoleMenus();
            seedUsers();
        }
    }

    private void seedMenus() {
        log.info("Seeding menus");
        Menu menu1 = createMenu("Dashboard ADMIN", "/admin/dashboard", "DashboardController", 1, null);
        Menu menu2 = createMenu("Dashboard User", "/user/dashboard", "DashboardController", 1, null);
        Menu menu3 = createMenu("Data Transaksi", "#", "Transaksi", 1, null);
        List<Menu> menus = List.of(menu1, menu2, menu3);
        menuRepo.saveAll(menus);

        Menu menuParent = menuRepo.findByName("Data Transaksi");
        if(menuParent != null) {
            Menu menuChild = createMenu("Data Invoice", "/user/invoice", "DataInvoice", 2, menuParent.getId());
            menuRepo.save(menuChild);
        }

        log.info("Finished seeding menus");
    }

    private void seedRoleMenus() {
        log.info("Seeding role menus");
        Menu menuAdmin = menuRepo.findByName("Dashboard ADMIN");
        Menu menuUser1 = menuRepo.findByName("Dashboard User");
        Menu menuUser2 = menuRepo.findByName("Data Transaksi");
        Menu menuUser3 = menuRepo.findByName("Data Invoice");
        if(menuAdmin != null || menuUser1 != null || menuUser2 != null || menuUser3 != null) {
            List<RoleMenu> roleMenus = List.of(
                    createRoleMenu(Role.ADMIN, menuAdmin),
                    createRoleMenu(Role.USER, menuUser1),
                    createRoleMenu(Role.USER, menuUser2),
                    createRoleMenu(Role.USER, menuUser3));
            roleMenuRepo.saveAll(roleMenus);
        }
        log.info("Finished seeding role menus");
    }

    private void seedUsers() {
        log.info("Seeding users");
        User user = createUser("user","user@gmail.com", "password", Role.USER);
        User admin = createUser("admin","admin@gmail.com", "password", Role.ADMIN);
        List<User> users = List.of(user, admin);
        userRepo.saveAll(users);
        log.info("Finished seeding users");
    }

    private Menu createMenu(String name, String url, String classLink, int sortOrder, UUID parentId) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setUrlink(url);
        menu.setClasslink(classLink);
        menu.setIsDeleted(false);
        menu.setCreatedBy("System");
        menu.setModifiedBy("System");
        menu.setParentId(parentId);
        menu.setCreatedDate(new java.util.Date());
        menu.setModifiedDate(new java.util.Date());
        menu.setSortOrder(sortOrder);
        return menu;
    }

    private RoleMenu createRoleMenu(Role role, Menu menu) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRole(role);
        roleMenu.setMenu(menu);
        roleMenu.setCreatedBy("System");
        roleMenu.setModifiedBy("System");
        roleMenu.setCreatedDate(new java.util.Date());
        roleMenu.setModifiedDate(new java.util.Date());
        roleMenu.setIsDeleted(false);
        return roleMenu;
    }

    private User createUser(String fullName,String email,String password, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setDeleted(false);
        user.setCreatedBy("System");
        user.setModifiedBy("System");
        user.setCreatedDate(new java.util.Date());
        user.setModifiedDate(new java.util.Date());
        return user;
    }
}
