package com.alexistdev.geobill.models.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserTest {

    @Test
    void whenUserHasRoleThenGetAuthoritiesReturnsCorrectAuthorities() {
        // Arrange
        Role role = Role.ADMIN;
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(role);

        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities, "Authorities should not be null");
        assertEquals(1, authorities.size(), "User should have one authority");
        assertEquals(new SimpleGrantedAuthority(role.name()), authorities.iterator().next(),
                "Authority should match the user's role");
    }

    @Test
    void whenUserRoleIsNullThenGetAuthoritiesReturnsEmptyAuthorities() {
        // Arrange
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(null);

        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities, "Authorities should not be null");
        assertEquals(Collections.emptyList(), authorities, "User with no role should have no authorities");
    }

    @Test
    void whenUserHasDifferentRoleThenGetAuthoritiesReturnsCorrectAuthority() {
        // Arrange
        Role role = Role.USER; // Assume Role is an enum value representing USER
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test User");
        user.setEmail("user@example.com");
        user.setPassword("securepassword");
        user.setRole(role);

        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities, "Authorities should not be null");
        assertEquals(1, authorities.size(), "User should have one authority");
        assertEquals(new SimpleGrantedAuthority(role.name()), authorities.iterator().next(),
                "Authority should match the user's role");
    }
}
