package com.alexistdev.geobill.seeder.user;

import com.alexistdev.geobill.models.entity.Role;

import java.util.List;

import static com.alexistdev.geobill.seeder.user.UserDefinition.user;

/**
 * The single place where the development accounts are declared.
 *
 * <p>They all share {@link #DEFAULT_PASSWORD}.</p>
 */
public final class UserCatalog {

    public static final String DEFAULT_PASSWORD = "password";

    private UserCatalog() {
    }

    public static List<UserDefinition> users() {
        return List.of(
                user("user", "user@gmail.com", Role.USER),
                user("user2", "user2@gmail.com", Role.USER),
                user("staff", "staff@gmail.com", Role.STAFF),
                user("admin", "admin@gmail.com", Role.ADMIN)
        );
    }
}
