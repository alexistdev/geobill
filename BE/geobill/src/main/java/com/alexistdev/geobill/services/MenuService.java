package com.alexistdev.geobill.services;

import com.alexistdev.geobill.dto.MenuDTO;
import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.repository.MenuRepo;
import com.alexistdev.geobill.models.repository.RoleMenuRepo;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return menus;
    }

    public Menu getMenuByName(String name) {
        Optional<Menu> menu = menuRepo.findByName(name);
        return menu.orElse(null);
    }


    public Optional<Menu> findById(UUID id) {
        return menuRepo.findById(id);
    }

    @Transactional
    public Menu createMenu(MenuDTO menuDTO) {
        Menu menu = new Menu();
        menu.setName(menuDTO.getName());
        menu.setUrlink(menuDTO.getUrlink());
        menu.setClasslink(menuDTO.getClasslink());
        menu.setIcon(menuDTO.getIcon());
        menu.setTypeMenu(menuDTO.getTypeMenu());

        // Parse and set sort order
        if (menuDTO.getSortOrder() != null && !menuDTO.getSortOrder().isEmpty()) {
            menu.setSortOrder(Integer.parseInt(menuDTO.getSortOrder()));
        }

        return menuRepo.save(menu);
    }

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
        menu.setTypeMenu(menuDTO.getTypeMenu());

        // Parse and set sort order
        if (menuDTO.getSortOrder() != null && !menuDTO.getSortOrder().isEmpty()) {
            menu.setSortOrder(Integer.parseInt(menuDTO.getSortOrder()));
        }

        return menuRepo.save(menu);
    }

    public List<Menu> getAllMenus() {
        return menuRepo.findAll();
    }

    @Transactional
    public void deleteMenu(UUID id) {
        menuRepo.deleteById(id);
    }
}
