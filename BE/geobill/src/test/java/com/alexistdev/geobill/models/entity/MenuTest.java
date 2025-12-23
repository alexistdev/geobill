package com.alexistdev.geobill.models.entity;

import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

public class MenuTest {

    public Menu menu;

    @BeforeEach
    void setUp() {
        menu = new Menu();
        menu.setId(new UUID(1,1));
        menu.setName("Dashboard");
        menu.setUrlink("/dashboard");
        menu.setClasslink("DashboardController");
        menu.setCreatedDate(new java.util.Date());
        menu.setModifiedDate(new java.util.Date());
        menu.setDeleted(false);
        menu.setCreatedBy("System");
        menu.setModifiedBy("System");
    }


}
