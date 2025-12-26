package com.alexistdev.geobill.utils;

import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.repository.MenuRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private MenuRepo menuRepo;

    @Override
    public void run(String... args) throws Exception {
        if(menuRepo.count() == 0) { //check if the menu table is empty (not seeded)
            seedMenus();
        }
    }

    private void seedMenus() {
        log.info("Seeding menus");
        Menu menu = new Menu();
        menu.setName("Dashboard");
        menu.setUrlink("/dashboard");
        menu.setClasslink("DashboardController");
        menu.setIsDeleted(false);
        menu.setCreatedBy("System");
        menu.setModifiedBy("System");
        menu.setCreatedDate(new java.util.Date());
        menu.setModifiedDate(new java.util.Date());
        menu.setSortOrder(1);
        menuRepo.save(menu);
        log.info("Finished seeding menus");
    }
}
