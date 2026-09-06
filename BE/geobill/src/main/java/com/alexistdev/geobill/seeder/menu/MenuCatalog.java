package com.alexistdev.geobill.seeder.menu;

import com.alexistdev.geobill.models.entity.Role;

import java.util.List;

import static com.alexistdev.geobill.seeder.menu.MenuDefinition.group;
import static com.alexistdev.geobill.seeder.menu.MenuDefinition.menu;
import static com.alexistdev.geobill.seeder.menu.MenuSection.section;

/**
 * The single place where the application menus are declared.
 *
 * <p>Adding a menu means adding one line below:</p>
 * <ul>
 *   <li>{@code menu(code, name, url, icon)} for a clickable entry;</li>
 *   <li>{@code group(code, name, icon, ...children)} for a parent that only opens a submenu;</li>
 *   <li>{@code .forRoles(Role.ADMIN, Role.STAFF)} on any entry that needs roles other than the
 *       section default.</li>
 * </ul>
 *
 * <p>Sort order follows the order written here, the parent link follows the nesting, and the
 * role_menu rows are generated from the section. The only rule is that every {@code code} must be
 * unique; {@link MenuSeeder} fails fast on start-up if it is not.</p>
 */
public final class MenuCatalog {

    private MenuCatalog() {
    }

    public static List<MenuSection> sections() {
        return List.of(adminMenus(), userMenus());
    }

    private static MenuSection adminMenus() {
        return section(MenuType.ADMIN, Role.ADMIN,
                menu("ADM_DASHBOARD", "Dashboard", "/admin/dashboard", "bx bx-home-alt"),
                group("ADM_MASTER", "Master Data", "bx bx-book-alt",
                        menu("ADM_PRODUCT_TYPE", "Product Type", "/admin/product_type", "bx bx-server"),
                        menu("ADM_PRODUCT", "Product", "/admin/product", "bx bx-server"),
                        menu("ADM_USER", "Users", "/admin/users", "bx bx-server")
                ),
                group("ADM_SUPPORT", "Support", "bx bx-book-alt",
                        menu("ADM_TICKET", "Ticket", "/admin/ticket", "bx bx-server")
                 )
        );
    }

    private static MenuSection userMenus() {
        return section(MenuType.USER, Role.USER,
                menu("USR_DASHBOARD", "Dashboard", "/users/dashboard", "bx bx-home-alt"),
                group("USR_SERVICE", "Services", "bx bx-collection",
                        menu("USR_SERVICE_LIST", "My Services", "/users/services", "bx bx-server"),
                        menu("USR_SERVICE_ORDER", "Order New Services", "/users/services/order", "bx bx-cart")
                ),
                group("USR_BILLING", "Billing", "bx bx-money",
                        menu("USR_INVOICE", "My Invoices", "/users/billings", "bx bx-barcode")
                ),
                group("USR_SUPPORT", "Support", "bx bx-headphone",
                        menu("USR_TICKET", "My Tickets", "/users/support/tickets", "bx bx-comment-edit")
                )
        );
    }
}
