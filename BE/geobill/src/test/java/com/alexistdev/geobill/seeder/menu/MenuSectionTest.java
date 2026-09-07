package com.alexistdev.geobill.seeder.menu;

import com.alexistdev.geobill.models.entity.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MenuSectionTest {

    private MenuDefinition dashboard;
    private MenuSection section;

    @BeforeEach
    void setUp() {
        dashboard = MenuDefinition.menu("ADM_DASHBOARD", "Dashboard", "/admin/dashboard", "bx bx-home-alt");
        section = MenuSection.section(MenuType.ADMIN, Role.ADMIN, dashboard);
    }

    @Test
    @Order(1)
    @DisplayName("1:Test Section Factory Method")
    void testSectionFactoryMethod() {
        Assertions.assertEquals(MenuType.ADMIN, section.type());
        Assertions.assertEquals(Set.of(Role.ADMIN), section.defaultRoles());
        Assertions.assertEquals(1, section.items().size());
        Assertions.assertEquals(dashboard, section.items().getFirst());
    }

    @Test
    @Order(2)
    @DisplayName("2:Test RolesOf Returns The Section Default")
    void testRolesOfReturnsDefault() {
        Assertions.assertEquals(Set.of(Role.ADMIN), section.rolesOf(dashboard));
    }

    @Test
    @Order(3)
    @DisplayName("3:Test RolesOf Returns The Menu Override")
    void testRolesOfReturnsOverride() {
        MenuDefinition shared = dashboard.forRoles(Role.ADMIN, Role.STAFF);

        Assertions.assertEquals(Set.of(Role.ADMIN, Role.STAFF), section.rolesOf(shared));
    }

    @Test
    @Order(4)
    @DisplayName("4:Test Section Without Items")
    void testSectionWithoutItems() {
        MenuSection empty = MenuSection.section(MenuType.USER, Role.USER);

        Assertions.assertEquals(MenuType.USER, empty.type());
        Assertions.assertTrue(empty.items().isEmpty());
        Assertions.assertEquals(Set.of(Role.USER), empty.defaultRoles());
    }
}
