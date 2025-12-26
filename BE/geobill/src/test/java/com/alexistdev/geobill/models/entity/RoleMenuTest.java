package com.alexistdev.geobill.models.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class RoleMenuTest {

    private RoleMenu roleMenu;
    private Menu menu;
    private Validator validator;

    @BeforeEach
    void setUp() {
        roleMenu = new RoleMenu();
        menu = new Menu();
        menu.setName("Dashboard");
        menu.setUrlink("/dashboard");
        menu.setSortOrder(1);
        menu.setIsDeleted(false);
        menu.setCreatedBy("System");
        menu.setModifiedDate(new java.util.Date());
        menu.setCreatedDate(new java.util.Date());
        menu.setModifiedBy("System");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

    }

    @Test
    @DisplayName("Test Getter And Setter")
    void testGetterAndSetter() {
        roleMenu.setRole(Role.USER);
        roleMenu.setMenu(menu);
        Assertions.assertEquals(Role.USER,roleMenu.getRole());
        Assertions.assertEquals(menu,roleMenu.getMenu());
    }

    @Test
    @DisplayName("Test Inherited Audit Fields")
    void testInheritedAuditFields() {
        String createdBy = "System";
        String modifiedBy = "System";
        Date dateCreatedModified = new java.util.Date();
        roleMenu.setCreatedBy(createdBy);
        roleMenu.setModifiedBy(modifiedBy);
        roleMenu.setCreatedDate(dateCreatedModified);
        roleMenu.setModifiedDate(dateCreatedModified);
        Assertions.assertEquals(createdBy,roleMenu.getCreatedBy());
        Assertions.assertEquals(modifiedBy,roleMenu.getModifiedBy());
        Assertions.assertEquals(dateCreatedModified,roleMenu.getCreatedDate());
        Assertions.assertEquals(dateCreatedModified,roleMenu.getModifiedDate());
    }

    @Test
    @DisplayName("Test IsDeleted Default Value")
    void testIsDeletedDefaultValue() {
        Assertions.assertFalse(roleMenu.getIsDeleted());
    }

    @Test
    @DisplayName("Test IsDeleted Setter")
    void testIsDeletedSetter() {
        roleMenu.setIsDeleted(true);
        Assertions.assertTrue(roleMenu.getIsDeleted());
    }

    @Test
    @DisplayName("Test Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(Serializable.class,roleMenu);
    }

    @Test
    @DisplayName("Test RoleMenu Entity")
    void testRoleMenuEntity() {
        Assertions.assertInstanceOf(RoleMenu.class,new RoleMenu());
    }

    @Test
    @DisplayName("Test Valid RoleMenu")
    void testValidRoleMenu() {
        roleMenu.setRole(Role.USER);
        roleMenu.setMenu(menu);
        roleMenu.setIsDeleted(false);
        roleMenu.setCreatedBy("System");
        roleMenu.setCreatedDate(new Date());
        roleMenu.setModifiedBy("System");
        roleMenu.setModifiedDate(new Date());

        Assertions.assertEquals(Role.USER,roleMenu.getRole());
        Assertions.assertEquals(menu,roleMenu.getMenu());
        Assertions.assertFalse(roleMenu.getIsDeleted());
        Assertions.assertNotNull(roleMenu.getCreatedDate());
        Assertions.assertNotNull(roleMenu.getModifiedDate());
        Assertions.assertNotNull(roleMenu.getCreatedBy());
        Assertions.assertNotNull(roleMenu.getModifiedBy());
    }

    @Test
    @DisplayName("Test Admin Enum")
    void testAdminEnum() {
        roleMenu.setRole(Role.ADMIN);
        Assertions.assertEquals(Role.ADMIN,roleMenu.getRole());
    }

    @Test
    @DisplayName("Test Staff Enum")
    void testStaffEnum() {
        roleMenu.setRole(Role.STAFF);
        Assertions.assertEquals(Role.STAFF,roleMenu.getRole());
    }

    @Test
    @DisplayName("Test User Enum")
    void testUserEnum() {
        roleMenu.setRole(Role.USER);
        Assertions.assertEquals(Role.USER,roleMenu.getRole());
    }

    @Test
    @DisplayName("Test Role is Required")
    void testRoleIsRequired() {
        roleMenu.setMenu(menu);
        roleMenu.setRole(null);

        Set<ConstraintViolation<RoleMenu>> violations = validator.validate(roleMenu);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Test Menu is Required")
    void testMenuIsRequired() {
        roleMenu.setMenu(null);
        roleMenu.setRole(Role.USER);

        Set<ConstraintViolation<RoleMenu>> violations = validator.validate(roleMenu);
        Assertions.assertFalse(violations.isEmpty());
    }
}
