package com.alexistdev.geobill.seeder.user;

import com.alexistdev.geobill.models.entity.Role;

/**
 * One seed account as written in {@link UserCatalog}.
 */
public record UserDefinition(String fullName, String email, Role role) {

    public static UserDefinition user(String fullName, String email, Role role) {
        return new UserDefinition(fullName, email, role);
    }
}
