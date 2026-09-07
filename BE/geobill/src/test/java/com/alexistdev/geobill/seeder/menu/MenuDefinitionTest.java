package com.alexistdev.geobill.seeder.menu;

import com.alexistdev.geobill.models.entity.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MenuDefinitionTest {

    @Test
    @Order(1)
    @DisplayName("1:Test Clickable Menu Entry")
    void testMenuEntry() {
        MenuDefinition definition =
                MenuDefinition.menu("ADM_DASHBOARD", "Dashboard", "/admin/dashboard", "bx bx-home-alt");

        Assertions.assertEquals("ADM_DASHBOARD", definition.code());
        Assertions.assertEquals("Dashboard", definition.name());
        Assertions.assertEquals("/admin/dashboard", definition.url());
        Assertions.assertEquals("bx bx-home-alt", definition.icon());
        Assertions.assertTrue(definition.roles().isEmpty());
        Assertions.assertTrue(definition.children().isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Group Menu Carries Its Children And No Url")
    void testGroupEntry() {
        MenuDefinition child = MenuDefinition.menu("ADM_PRODUCT", "Product", "/admin/product", "bx bx-server");
        MenuDefinition group = MenuDefinition.group("ADM_MASTER", "Master Data", "bx bx-book-alt", child);

        Assertions.assertEquals("ADM_MASTER", group.code());
        Assertions.assertEquals("Master Data", group.name());
        Assertions.assertEquals("#", group.url());
        Assertions.assertEquals(1, group.children().size());
        Assertions.assertEquals(child, group.children().getFirst());
    }

    @Test
    @Order(3)
    @DisplayName("3:Test Group Without Children")
    void testGroupWithoutChildren() {
        MenuDefinition group = MenuDefinition.group("ADM_EMPTY", "Empty", "bx bx-book-alt");

        Assertions.assertTrue(group.children().isEmpty());
        Assertions.assertEquals("#", group.url());
    }

    @Test
    @Order(4)
    @DisplayName("4:Test ForRoles Overrides The Roles Only")
    void testForRolesOverridesRoles() {
        MenuDefinition definition =
                MenuDefinition.menu("ADM_USER", "Users", "/admin/users", "bx bx-server");

        MenuDefinition withRoles = definition.forRoles(Role.ADMIN, Role.STAFF);

        Assertions.assertEquals(Set.of(Role.ADMIN, Role.STAFF), withRoles.roles());
        Assertions.assertEquals(definition.code(), withRoles.code());
        Assertions.assertEquals(definition.name(), withRoles.name());
        Assertions.assertEquals(definition.url(), withRoles.url());
        Assertions.assertEquals(definition.icon(), withRoles.icon());
        Assertions.assertEquals(definition.children(), withRoles.children());
    }

    @Test
    @Order(5)
    @DisplayName("5:Test ForRoles Leaves The Original Untouched")
    void testForRolesDoesNotMutateOriginal() {
        MenuDefinition definition =
                MenuDefinition.menu("ADM_USER", "Users", "/admin/users", "bx bx-server");

        definition.forRoles(Role.ADMIN);

        Assertions.assertTrue(definition.roles().isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("6:Test ForRoles Keeps The Children Of A Group")
    void testForRolesKeepsChildren() {
        MenuDefinition child = MenuDefinition.menu("ADM_TICKET", "Ticket", "/admin/ticket", "bx bx-server");
        MenuDefinition group = MenuDefinition.group("ADM_SUPPORT", "Support", "bx bx-book-alt", child);

        MenuDefinition withRoles = group.forRoles(Role.STAFF);

        Assertions.assertEquals(1, withRoles.children().size());
        Assertions.assertEquals(Set.of(Role.STAFF), withRoles.roles());
    }
}
