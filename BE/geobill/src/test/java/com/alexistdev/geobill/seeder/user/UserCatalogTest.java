package com.alexistdev.geobill.seeder.user;

import com.alexistdev.geobill.models.entity.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserCatalogTest {

    @Test
    @Order(1)
    @DisplayName("1:Test Catalog Declares Four Accounts")
    void testCatalogSize() {
        List<UserDefinition> users = UserCatalog.users();

        Assertions.assertNotNull(users);
        Assertions.assertEquals(4, users.size());
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Catalog Declares One Account Per Expected Email")
    void testCatalogEmails() {
        Set<String> emails = UserCatalog.users().stream()
                .map(UserDefinition::email)
                .collect(Collectors.toSet());

        Assertions.assertEquals(
                Set.of("user@gmail.com", "user2@gmail.com", "staff@gmail.com", "admin@gmail.com"),
                emails);
    }

    @Test
    @Order(3)
    @DisplayName("3:Test Emails Are Unique")
    void testEmailsAreUnique() {
        List<UserDefinition> users = UserCatalog.users();

        long distinct = users.stream().map(UserDefinition::email).distinct().count();

        Assertions.assertEquals(users.size(), distinct);
    }

    @Test
    @Order(4)
    @DisplayName("4:Test Roles Of The Declared Accounts")
    void testRoles() {
        List<UserDefinition> users = UserCatalog.users();

        Assertions.assertEquals(Role.USER, users.get(0).role());
        Assertions.assertEquals("user", users.get(0).fullName());
        Assertions.assertEquals(Role.USER, users.get(1).role());
        Assertions.assertEquals(Role.STAFF, users.get(2).role());
        Assertions.assertEquals(Role.ADMIN, users.get(3).role());
    }

    @Test
    @Order(5)
    @DisplayName("5:Test Every Account Has A Full Name And An Email")
    void testEveryAccountIsComplete() {
        for (UserDefinition definition : UserCatalog.users()) {
            Assertions.assertNotNull(definition.fullName());
            Assertions.assertFalse(definition.fullName().isBlank());
            Assertions.assertTrue(definition.email().contains("@"));
            Assertions.assertNotNull(definition.role());
        }
    }

    @Test
    @Order(6)
    @DisplayName("6:Test Default Password")
    void testDefaultPassword() {
        Assertions.assertEquals("password", UserCatalog.DEFAULT_PASSWORD);
    }

    @Test
    @Order(7)
    @DisplayName("7:Test Catalog Is Immutable")
    void testCatalogIsImmutable() {
        List<UserDefinition> users = UserCatalog.users();

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> users.add(UserDefinition.user("hacker", "hacker@gmail.com", Role.ADMIN)));
    }

    @Test
    @Order(8)
    @DisplayName("8:Test Utility Class Cannot Be Instantiated")
    void testUtilityClassIsNotInstantiable() throws Exception {
        Constructor<UserCatalog> constructor = UserCatalog.class.getDeclaredConstructor();

        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Assertions.assertNotNull(constructor.newInstance());
    }
}
