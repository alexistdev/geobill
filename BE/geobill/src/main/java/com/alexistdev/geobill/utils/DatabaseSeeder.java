package com.alexistdev.geobill.utils;

import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.RoleMenu;
import com.alexistdev.geobill.models.repository.MenuRepo;
import com.alexistdev.geobill.models.repository.RoleMenuRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private MenuRepo menuRepo;

    @Autowired
    private RoleMenuRepo roleMenuRepo;

    @Override
    public void run(String... args) throws Exception {
        if (menuRepo.count() == 0) { // check if the menu table is empty (not seeded)
            seedMenus();
            seedRoleMenus();
        }
    }

    private void seedMenus() {
        log.info("Seeding menus");
        List<Menu> menus = List.of(
                createMenu("Dashboard User", "/users/dashboard", "DashboardController", 1),
                createMenu("Dashboard ADMIN", "/admin/dashboard", "DashboardAdminController", 1),
                createMenu("Master Data", "#", "MasterData", 1),
                createMenu("Data Menu", "/admin/menu", "DataMenu", 2));
        menuRepo.saveAll(menus);

        log.info("Finished seeding menus");
    }

    private void seedRoleMenus() {
        log.info("Seeding role menus");
        Menu menuAdmin1 = menuRepo.findByName("Dashboard ADMIN");
        Menu menuAdmin2 = menuRepo.findByName("Master Data");
        Menu menuAdmin3 = menuRepo.findByName("Data Menu");
        Menu menuUser = menuRepo.findByName("Dashboard User");

        List<RoleMenu> roleMenus = List.of(
                createRoleMenu(Role.ADMIN, menuAdmin1),
                createRoleMenu(Role.ADMIN, menuAdmin2),
                createRoleMenu(Role.ADMIN, menuAdmin3),
                createRoleMenu(Role.USER, menuUser));

        roleMenuRepo.saveAll(roleMenus);
        log.info("Finished seeding role menus");
    }

    private Menu createMenu(String name, String url, String classLink, int sortOrder) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setUrlink(url);
        menu.setClasslink(classLink);
        menu.setIsDeleted(false);
        menu.setCreatedBy("System");
        menu.setModifiedBy("System");
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
}
