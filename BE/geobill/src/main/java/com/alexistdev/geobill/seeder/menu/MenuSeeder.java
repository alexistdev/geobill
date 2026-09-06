package com.alexistdev.geobill.seeder.menu;

import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.RoleMenu;
import com.alexistdev.geobill.models.repository.MenuRepo;
import com.alexistdev.geobill.models.repository.RoleMenuRepo;
import com.alexistdev.geobill.seeder.SeedAudit;
import com.alexistdev.geobill.seeder.Seeder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Turns {@link MenuCatalog} into {@code tb_menus} and {@code tb_role_menus} rows.
 *
 * <p>Nothing here has to change when a menu is added; edit the catalog instead.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuSeeder implements Seeder {

    /** CSS class of a top level menu; children carry no class. */
    private static final String ROOT_CLASS_LINK = "menu-title d-flex align-items-center";
    private static final String CHILD_CLASS_LINK = "";

    private final MenuRepo menuRepo;
    private final RoleMenuRepo roleMenuRepo;

    @Override
    public String name() {
        return "menus";
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public boolean shouldRun() {
        return menuRepo.count() == 0;
    }

    @Override
    @Transactional
    public void seed() {
        List<MenuSection> sections = MenuCatalog.sections();
        verifyCodesAreUnique(sections);

        List<RoleMenu> roleMenus = new ArrayList<>();
        for (MenuSection section : sections) {
            saveLevel(section, section.items(), null, 0, roleMenus);
        }

        roleMenuRepo.saveAll(roleMenus);
        log.info("Seeded {} menu(s) and {} role menu(s)", menuRepo.count(), roleMenus.size());
    }

    /** Saves one level of the tree, then recurses into the children of each entry. */
    private void saveLevel(MenuSection section,
                           List<MenuDefinition> definitions,
                           UUID parentId,
                           int depth,
                           List<RoleMenu> roleMenus) {

        int sortOrder = 1;
        for (MenuDefinition definition : definitions) {
            Menu menu = menuRepo.save(toMenu(definition, section, parentId, sortOrder++, depth));

            for (Role role : section.rolesOf(definition)) {
                roleMenus.add(toRoleMenu(role, menu));
            }

            if (!definition.children().isEmpty()) {
                saveLevel(section, definition.children(), menu.getId(), depth + 1, roleMenus);
            }
        }
    }

    private Menu toMenu(MenuDefinition definition, MenuSection section, UUID parentId, int sortOrder, int depth) {
        Menu menu = new Menu();
        menu.setCode(definition.code());
        menu.setName(definition.name());
        menu.setUrlink(definition.url());
        menu.setIcon(definition.icon());
        menu.setClasslink(depth == 0 ? ROOT_CLASS_LINK : CHILD_CLASS_LINK);
        menu.setParentId(parentId);
        menu.setSortOrder(sortOrder);
        menu.setTypeMenu(section.type().value());
        return SeedAudit.stamp(menu);
    }

    private RoleMenu toRoleMenu(Role role, Menu menu) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRole(role);
        roleMenu.setMenu(menu);
        return SeedAudit.stamp(roleMenu);
    }

    /** A duplicated code would silently break {@code findByCode}, so fail on start-up instead. */
    private void verifyCodesAreUnique(List<MenuSection> sections) {
        Set<String> seen = new HashSet<>();
        for (MenuSection section : sections) {
            collectCodes(section.items(), seen);
        }
    }

    private void collectCodes(List<MenuDefinition> definitions, Set<String> seen) {
        for (MenuDefinition definition : definitions) {
            if (!seen.add(definition.code())) {
                throw new IllegalStateException("Duplicate menu code in MenuCatalog: " + definition.code());
            }
            collectCodes(definition.children(), seen);
        }
    }
}
