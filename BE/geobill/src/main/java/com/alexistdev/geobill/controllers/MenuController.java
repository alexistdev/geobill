package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.MenuDTO;
import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.services.MenuService;
import com.alexistdev.geobill.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService menuService;
    private final MessagesUtils messagesUtils;

    public MenuController(MenuService menuService, MessagesUtils messagesUtils) {
        this.menuService = menuService;
        this.messagesUtils = messagesUtils;
    }

    @GetMapping
    public ResponseEntity<List<Menu>> getAllMenus() {
        try {
            List<Menu> menus = menuService.getAllMenus();
            return ResponseEntity.ok(menus);
        } catch (Exception e) {
            log.error("Error fetching all menus", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Menu> getMenuById(@PathVariable String id) {
        try {
            UUID menuId = UUID.fromString(id);
            Optional<Menu> menu = menuService.findById(menuId);

            if (menu.isPresent()) {
                return ResponseEntity.ok(menu.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", id);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching menu by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createMenu(@RequestBody MenuDTO menuDTO) {
        try {
            Menu createdMenu = menuService.createMenu(menuDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMenu);
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating menu", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating menu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(messagesUtils.getMessage("menucontroller.create_failed"));
        }
    }

    /**
     * Update an existing menu
     * Example JSON request body:
     * {
     * "name": "Updated Dashboard",
     * "urlink": "/dashboard-new",
     * "icon": "new-icon",
     * "classlink": "nav-link-updated",
     * "sortOrder": "2",
     * "parent": "550e8400-e29b-41d4-a716-446655440000" // UUID of parent menu
     * (optional, null to make top-level)
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenu(@PathVariable String id, @RequestBody MenuDTO menuDTO) {
        try {
            UUID menuId = UUID.fromString(id);
            Menu updatedMenu = menuService.updateMenu(menuId, menuDTO);
            return ResponseEntity.ok(updatedMenu);
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating menu", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating menu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(messagesUtils.getMessage("menucontroller.update_failed"));
        }
    }

    /**
     * Delete a menu
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenu(@PathVariable String id) {
        try {
            UUID menuId = UUID.fromString(id);
            menuService.deleteMenu(menuId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", id);
            return ResponseEntity.badRequest().body(messagesUtils.getMessage("menucontroller.invalid_uuid"));
        } catch (Exception e) {
            log.error("Error deleting menu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(messagesUtils.getMessage("menucontroller.delete_failed"));
        }
    }
}
