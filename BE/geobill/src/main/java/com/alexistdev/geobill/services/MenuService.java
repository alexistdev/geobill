package com.alexistdev.geobill.services;

import com.alexistdev.geobill.dto.MenuDTO;
import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.repository.MenuRepo;
import com.alexistdev.geobill.models.repository.RoleMenuRepo;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MenuService {

    @Autowired
    private MenuRepo menuRepo;

    @Autowired
    private RoleMenuRepo roleMenuRepo;

    @Transactional
    public List<Menu> getMenusByRole(Role role) {
        List<Menu> menus = roleMenuRepo.findByRole(role).stream()
                .map(roleMenu -> roleMenu.getMenu())
                .collect(Collectors.toList());

        // force init of lazy loaded relationship (parent and children)
        for (Menu menu : menus) {
            if (menu.getParent() != null) {
                menu.getParent().getName();
            }
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                menu.getChildren().size();
            }
        }
        return menus;
    }

    public Menu getMenuByName(String name) {
        return menuRepo.findByName(name);
    }

    /**
     * Find menu by UUID
     */
    public Optional<Menu> findById(UUID id) {
        return menuRepo.findById(id);
    }

    /**
     * Create a new menu with parent relationship
     * 
     * @param menuDTO - DTO containing menu data with parent UUID as string
     * @return saved Menu entity
     */
    @Transactional
    public Menu createMenu(MenuDTO menuDTO) {
        Menu menu = new Menu();
        menu.setName(menuDTO.getName());
        menu.setUrlink(menuDTO.getUrlink());
        menu.setClasslink(menuDTO.getClasslink());
        menu.setIcon(menuDTO.getIcon());

        // Parse and set sort order
        if (menuDTO.getSortOrder() != null && !menuDTO.getSortOrder().isEmpty()) {
            menu.setSortOrder(Integer.parseInt(menuDTO.getSortOrder()));
        }

        // Handle parent relationship - convert UUID string to Menu object
        if (menuDTO.getParent() != null && !menuDTO.getParent().isEmpty()) {
            try {
                UUID parentId = UUID.fromString(menuDTO.getParent());
                Optional<Menu> parentMenu = menuRepo.findById(parentId);
                if (parentMenu.isPresent()) {
                    menu.setParent(parentMenu.get());
                } else {
                    log.warn("Parent menu with ID {} not found", parentId);
                    throw new IllegalArgumentException("Parent menu not found with ID: " + parentId);
                }
            } catch (IllegalArgumentException e) {
                log.error("Invalid parent UUID format: {}", menuDTO.getParent());
                throw new IllegalArgumentException("Invalid parent UUID format: " + menuDTO.getParent());
            }
        }

        return menuRepo.save(menu);
    }

    /**
     * Update an existing menu with parent relationship
     * 
     * @param id      - UUID of the menu to update
     * @param menuDTO - DTO containing updated menu data
     * @return updated Menu entity
     */
    @Transactional
    public Menu updateMenu(UUID id, MenuDTO menuDTO) {
        Optional<Menu> existingMenuOpt = menuRepo.findById(id);

        if (!existingMenuOpt.isPresent()) {
            throw new IllegalArgumentException("Menu not found with ID: " + id);
        }

        Menu menu = existingMenuOpt.get();
        menu.setName(menuDTO.getName());
        menu.setUrlink(menuDTO.getUrlink());
        menu.setClasslink(menuDTO.getClasslink());
        menu.setIcon(menuDTO.getIcon());

        // Parse and set sort order
        if (menuDTO.getSortOrder() != null && !menuDTO.getSortOrder().isEmpty()) {
            menu.setSortOrder(Integer.parseInt(menuDTO.getSortOrder()));
        }

        // Handle parent relationship - convert UUID string to Menu object
        if (menuDTO.getParent() != null && !menuDTO.getParent().isEmpty()) {
            try {
                UUID parentId = UUID.fromString(menuDTO.getParent());

                // Prevent circular reference - menu cannot be its own parent
                if (parentId.equals(id)) {
                    throw new IllegalArgumentException("Menu cannot be its own parent");
                }

                Optional<Menu> parentMenu = menuRepo.findById(parentId);
                if (parentMenu.isPresent()) {
                    menu.setParent(parentMenu.get());
                } else {
                    log.warn("Parent menu with ID {} not found", parentId);
                    throw new IllegalArgumentException("Parent menu not found with ID: " + parentId);
                }
            } catch (IllegalArgumentException e) {
                log.error("Invalid parent UUID format: {}", menuDTO.getParent());
                throw new IllegalArgumentException("Invalid parent UUID format: " + menuDTO.getParent());
            }
        } else {
            // If parent is null or empty, set parent to null (top-level menu)
            menu.setParent(null);
        }

        return menuRepo.save(menu);
    }

    /**
     * Get all menus
     */
    public List<Menu> getAllMenus() {
        return menuRepo.findAll();
    }

    /**
     * Delete menu by ID
     */
    @Transactional
    public void deleteMenu(UUID id) {
        menuRepo.deleteById(id);
    }
}
