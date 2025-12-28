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
        if(menuRepo.count() == 0) { //check if the menu table is empty (not seeded)
            seedMenus();
            seedRoleMenus();
        }
    }

    private void seedMenus() {
        log.info("Seeding menus");
        Menu menu1 = new Menu();
        menu1.setName("Dashboard User");
        menu1.setUrlink("/users/dashboard");
        menu1.setClasslink("DashboardController");
        menu1.setIsDeleted(false);
        menu1.setCreatedBy("System");
        menu1.setModifiedBy("System");
        menu1.setCreatedDate(new java.util.Date());
        menu1.setModifiedDate(new java.util.Date());
        menu1.setSortOrder(1);
        menuRepo.save(menu1);

        Menu menu2 = new Menu();
        menu2.setName("Dashboard ADMIN");
        menu2.setUrlink("/admin/dashboard");
        menu2.setClasslink("DashboardController");
        menu2.setIsDeleted(false);
        menu2.setCreatedBy("System");
        menu2.setModifiedBy("System");
        menu2.setCreatedDate(new java.util.Date());
        menu2.setModifiedDate(new java.util.Date());
        menu2.setSortOrder(1);
        menuRepo.save(menu2);

        log.info("Finished seeding menus");
    }

    private void seedRoleMenus() {
        log.info("Seeding role menus");
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRole(Role.ADMIN);

        Menu menuAdmin = menuRepo.findByName("Dashboard ADMIN");
        roleMenu.setMenu(menuAdmin);
        roleMenu.setCreatedBy("System");
        roleMenu.setModifiedBy("System");
        roleMenu.setCreatedDate(new java.util.Date());
        roleMenu.setModifiedDate(new java.util.Date());

        roleMenuRepo.save(roleMenu);
        Menu menuUser = menuRepo.findByName("Dashboard User");
        RoleMenu roleMenu2 = new RoleMenu();

        roleMenu2.setRole(Role.USER);
        roleMenu2.setMenu(menuUser);
        roleMenu2.setCreatedBy("System");
        roleMenu2.setModifiedBy("System");
        roleMenu2.setCreatedDate(new java.util.Date());
        roleMenu2.setModifiedDate(new java.util.Date());
        roleMenuRepo.save(roleMenu2);
        log.info("Finished seeding role menus");
    }
}
