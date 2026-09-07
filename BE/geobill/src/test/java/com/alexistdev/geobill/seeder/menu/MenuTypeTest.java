package com.alexistdev.geobill.seeder.menu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MenuTypeTest {

    @Test
    @Order(1)
    @DisplayName("1:Test Declared Menu Types")
    void testDeclaredMenuTypes() {
        Assertions.assertEquals(2, MenuType.values().length);
        Assertions.assertEquals(MenuType.ADMIN, MenuType.valueOf("ADMIN"));
        Assertions.assertEquals(MenuType.USER, MenuType.valueOf("USER"));
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Stored Value Of Each Menu Type")
    void testStoredValues() {
        Assertions.assertEquals(1, MenuType.ADMIN.value());
        Assertions.assertEquals(2, MenuType.USER.value());
    }

    @Test
    @Order(3)
    @DisplayName("3:Test Values Are Unique")
    void testValuesAreUnique() {
        long distinct = java.util.Arrays.stream(MenuType.values()).mapToInt(MenuType::value).distinct().count();

        Assertions.assertEquals(MenuType.values().length, distinct);
    }
}
