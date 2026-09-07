package com.alexistdev.geobill.seeder.user;

import com.alexistdev.geobill.models.entity.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDefinitionTest {

    @Test
    @Order(1)
    @DisplayName("1:Test Factory Method Fills Every Field")
    void testFactoryMethod() {
        UserDefinition definition = UserDefinition.user("admin", "admin@gmail.com", Role.ADMIN);

        Assertions.assertEquals("admin", definition.fullName());
        Assertions.assertEquals("admin@gmail.com", definition.email());
        Assertions.assertEquals(Role.ADMIN, definition.role());
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Constructor Matches Factory Method")
    void testConstructorMatchesFactoryMethod() {
        UserDefinition fromFactory = UserDefinition.user("user", "user@gmail.com", Role.USER);
        UserDefinition fromConstructor = new UserDefinition("user", "user@gmail.com", Role.USER);

        Assertions.assertEquals(fromConstructor, fromFactory);
        Assertions.assertEquals(fromConstructor.hashCode(), fromFactory.hashCode());
    }

    @Test
    @Order(3)
    @DisplayName("3:Test Definitions With Different Roles Are Not Equal")
    void testDifferentRolesAreNotEqual() {
        UserDefinition staff = UserDefinition.user("staff", "staff@gmail.com", Role.STAFF);
        UserDefinition user = UserDefinition.user("staff", "staff@gmail.com", Role.USER);

        Assertions.assertNotEquals(staff, user);
    }
}
