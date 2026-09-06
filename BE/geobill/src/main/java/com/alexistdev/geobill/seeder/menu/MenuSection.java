package com.alexistdev.geobill.seeder.menu;

import com.alexistdev.geobill.models.entity.Role;

import java.util.List;
import java.util.Set;

/**
 * A group of menus sharing one {@link MenuType} and one set of default roles.
 */
public record MenuSection(
        MenuType type,
        Set<Role> defaultRoles,
        List<MenuDefinition> items
) {

    public static MenuSection section(MenuType type, Role defaultRole, MenuDefinition... items) {
        return new MenuSection(type, Set.of(defaultRole), List.of(items));
    }

    /** Roles that may see the given menu: its own override, or the section default. */
    public Set<Role> rolesOf(MenuDefinition definition) {
        return definition.roles().isEmpty() ? defaultRoles : definition.roles();
    }
}
