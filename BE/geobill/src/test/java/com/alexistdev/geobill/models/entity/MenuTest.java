package com.alexistdev.geobill.models.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.fail;

public class MenuTest {

    public Menu menu;
    private UUID id;

    @BeforeEach
    void setUp() {
        menu = new Menu();
        id = UUID.randomUUID();
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

    @Test
    @DisplayName("Should return id of saved menu")
    void testSendAndGetId() {
        menu.setId(id);
        Assertions.assertEquals(id, menu.getId());
    }

    @Test
    @DisplayName("Should return name of saved menu")
    void testSendAndGetName() {
        String name = "Dashboard";
        menu.setName(name);
        Assertions.assertEquals(name, menu.getName());
    }

    @Test
    @DisplayName("Should return classlink of saved menu")
    void testGetAndSetClasslink() {
        String classlink = "SettingsController";
        menu.setClasslink(classlink);
        Assertions.assertEquals(classlink, menu.getClasslink());
    }

    @Test
    @DisplayName("Should verify Menu extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class,menu);
    }

    @Test
    @DisplayName("Ensure menu is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(Serializable.class,menu);
    }

    @Test
    @DisplayName("Should verify id is UUID")
    void testIdTypeIsUUID() {
        try{
            Field field = Menu.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException  e){
            fail("id field should exist in Menu class");
        }
    }

    @Test
    @DisplayName("Should allow setting a valid name")
    void testSetNameWithValidValue() {
        String newName = "Settings";
        menu.setName(newName);
        Assertions.assertEquals(newName, menu.getName());
    }

    @Test
    @DisplayName("Should handle setting name to null")
    void testSetNameWithNullValue() {
        menu.setName(null);
        Assertions.assertNull(menu.getName());
    }

    @Test
    @DisplayName("Should handle setting urlink to null")
    void testSetUrlinkWithNullValue() {
        menu.setUrlink(null);
        Assertions.assertNull(menu.getUrlink());
    }

    @Test
    @DisplayName("Should handle setting classlink to null")
    void testSetClasslinkWithNullValue() {
        menu.setClasslink(null);
        Assertions.assertNull(menu.getClasslink());
    }
}
