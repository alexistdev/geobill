package com.alexistdev.geobill.seeder.menu;

import com.alexistdev.geobill.models.entity.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MenuCatalogTest {

    private static List<MenuDefinition> flatten(List<MenuDefinition> definitions) {
        List<MenuDefinition> all = new ArrayList<>();
        for (MenuDefinition definition : definitions) {
            all.add(definition);
            all.addAll(flatten(definition.children()));
        }
        return all;
    }

    private static List<MenuDefinition> allDefinitions() {
        List<MenuDefinition> all = new ArrayList<>();
        for (MenuSection section : MenuCatalog.sections()) {
            all.addAll(flatten(section.items()));
        }
        return all;
    }

    @Test
    @Order(1)
    @DisplayName("1:Test Catalog Declares An Admin And A User Section")
    void testSections() {
        List<MenuSection> sections = MenuCatalog.sections();

        Assertions.assertEquals(2, sections.size());
        Assertions.assertEquals(MenuType.ADMIN, sections.get(0).type());
        Assertions.assertEquals(Set.of(Role.ADMIN), sections.get(0).defaultRoles());
        Assertions.assertEquals(MenuType.USER, sections.get(1).type());
        Assertions.assertEquals(Set.of(Role.USER), sections.get(1).defaultRoles());
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Menu Codes Are Unique")
    void testCodesAreUnique() {
        Set<String> seen = new HashSet<>();

        for (MenuDefinition definition : allDefinitions()) {
            Assertions.assertTrue(seen.add(definition.code()),
                    "Duplicate menu code in MenuCatalog: " + definition.code());
        }
    }

    @Test
    @Order(3)
    @DisplayName("3:Test Every Menu Has A Code, A Name, A Url And An Icon")
    void testEveryMenuIsComplete() {
        for (MenuDefinition definition : allDefinitions()) {
            Assertions.assertNotNull(definition.code());
            Assertions.assertFalse(definition.code().isBlank());
            Assertions.assertNotNull(definition.name());
            Assertions.assertFalse(definition.name().isBlank());
            Assertions.assertNotNull(definition.url());
            Assertions.assertFalse(definition.url().isBlank());
            Assertions.assertNotNull(definition.icon());
        }
    }

    @Test
    @Order(4)
    @DisplayName("4:Test Admin Section Content")
    void testAdminSection() {
        MenuSection admin = MenuCatalog.sections().getFirst();

        Assertions.assertEquals(3, admin.items().size());
        Assertions.assertEquals("ADM_DASHBOARD", admin.items().get(0).code());
        Assertions.assertEquals("/admin/dashboard", admin.items().get(0).url());
        Assertions.assertEquals("ADM_MASTER", admin.items().get(1).code());
        Assertions.assertEquals(3, admin.items().get(1).children().size());
        Assertions.assertEquals("ADM_SUPPORT", admin.items().get(2).code());
        Assertions.assertEquals(1, admin.items().get(2).children().size());
    }

    @Test
    @Order(5)
    @DisplayName("5:Test User Section Content")
    void testUserSection() {
        MenuSection user = MenuCatalog.sections().get(1);

        Assertions.assertEquals(4, user.items().size());
        Assertions.assertEquals("USR_DASHBOARD", user.items().get(0).code());
        Assertions.assertEquals("USR_SERVICE", user.items().get(1).code());
        Assertions.assertEquals(2, user.items().get(1).children().size());
        Assertions.assertEquals("USR_BILLING", user.items().get(2).code());
        Assertions.assertEquals("USR_SUPPORT", user.items().get(3).code());
    }

    @Test
    @Order(6)
    @DisplayName("6:Test Group Menus Have No Real Url")
    void testGroupMenusHaveNoUrl() {
        for (MenuDefinition definition : allDefinitions()) {
            if (!definition.children().isEmpty()) {
                Assertions.assertEquals("#", definition.url(),
                        "Group menu should not be clickable: " + definition.code());
            }
        }
    }

    @Test
    @Order(7)
    @DisplayName("7:Test Admin Codes Start With ADM And User Codes With USR")
    void testCodePrefixes() {
        for (MenuDefinition definition : flatten(MenuCatalog.sections().get(0).items())) {
            Assertions.assertTrue(definition.code().startsWith("ADM_"), definition.code());
        }
        for (MenuDefinition definition : flatten(MenuCatalog.sections().get(1).items())) {
            Assertions.assertTrue(definition.code().startsWith("USR_"), definition.code());
        }
    }

    @Test
    @Order(8)
    @DisplayName("8:Test Sections Are Immutable")
    void testSectionsAreImmutable() {
        List<MenuSection> sections = MenuCatalog.sections();

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> sections.add(MenuSection.section(MenuType.USER, Role.USER)));
    }

    @Test
    @Order(9)
    @DisplayName("9:Test Utility Class Cannot Be Instantiated")
    void testUtilityClassIsNotInstantiable() throws Exception {
        Constructor<MenuCatalog> constructor = MenuCatalog.class.getDeclaredConstructor();

        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Assertions.assertNotNull(constructor.newInstance());
    }
}
