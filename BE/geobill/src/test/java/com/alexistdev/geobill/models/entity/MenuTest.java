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
        menu.setIcon("home");
        menu.setSortOrder(1);
        menu.setTypeMenu(1);
        menu.setParentId(UUID.randomUUID());
        menu.setCreatedBy("System");
        menu.setModifiedBy("System");
        menu.setCreatedDate(new java.util.Date());
        menu.setModifiedDate(new java.util.Date());
        menu.setDeleted(false);

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
    @DisplayName("Should return typemenu of saved menu")
    void testGetAndSetTypeMenu() {
        int typeMenu = 1;
        menu.setTypeMenu(typeMenu);
        Assertions.assertEquals(typeMenu, menu.getTypeMenu());
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

    @Test
    @DisplayName("Should return urlink of saved menu")
    void testGetAndSetUrlink() {
        String urlink = "/settings";
        menu.setUrlink(urlink);
        Assertions.assertEquals(urlink, menu.getUrlink());
    }

    @Test
    @DisplayName("Should return icon of saved menu")
    void testGetAndSetIcon() {
        String icon = "settings";
        menu.setIcon(icon);
        Assertions.assertEquals(icon, menu.getIcon());
    }

    @Test
    @DisplayName("Should return sortOrder of saved menu")
    void testGetAndSetSortOrder() {
        int sortOrder = 2;
        menu.setSortOrder(sortOrder);
        Assertions.assertEquals(sortOrder, menu.getSortOrder());
    }

    @Test
    @DisplayName("Should return parentId of saved menu")
    void testGetAndSetParentId() {
        UUID parentId = UUID.randomUUID();
        menu.setParentId(parentId);
        Assertions.assertEquals(parentId, menu.getParentId());
    }

    @Test
    @DisplayName("Should verify parentId is UUID")
    void testParentIdTypeIsUUID() {
        try{
            Field field = Menu.class.getDeclaredField("parentId");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException  e){
            fail("parentId field should exist in Menu class");
        }
    }

    @Test
    @DisplayName("Should verify default value of sortOrder is 0")
    void testDefaultSortOrderValue() {
        Menu newMenu = new Menu();
        Assertions.assertEquals(0, newMenu.getSortOrder());
    }

    @Test
    @DisplayName("Should handle setting icon to null")
    void testSetIconWithNullValue() {
        menu.setIcon(null);
        Assertions.assertNull(menu.getIcon());
    }

}
