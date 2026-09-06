package com.alexistdev.geobill.seeder.menu;

import com.alexistdev.geobill.models.entity.Role;

import java.util.List;
import java.util.Set;

/**
 * One menu entry as written in {@link MenuCatalog}.
 *
 * <p>Only what really varies is declared here. Sort order comes from the position in the list,
 * the CSS class comes from the depth, and the roles are inherited from the section unless
 * {@link #forRoles(Role...)} overrides them.</p>
 */
public record MenuDefinition(
        String code,
        String name,
        String url,
        String icon,
        Set<Role> roles,
        List<MenuDefinition> children
) {

    /** URL used by a parent menu that only opens a submenu. */
    private static final String NO_URL = "#";

    /** A clickable menu entry. */
    public static MenuDefinition menu(String code, String name, String url, String icon) {
        return new MenuDefinition(code, name, url, icon, Set.of(), List.of());
    }

    /** A parent menu that only groups its children. */
    public static MenuDefinition group(String code, String name, String icon, MenuDefinition... children) {
        return new MenuDefinition(code, name, NO_URL, icon, Set.of(), List.of(children));
    }

    /** Overrides the roles inherited from the section, e.g. a menu shared by ADMIN and STAFF. */
    public MenuDefinition forRoles(Role... roles) {
        return new MenuDefinition(code, name, url, icon, Set.of(roles), children);
    }
}
