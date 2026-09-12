package com.alexistdev.geobill.services;

import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.RoleMenu;
import com.alexistdev.geobill.models.repository.MenuRepo;
import com.alexistdev.geobill.models.repository.RoleMenuRepo;
import jakarta.transaction.Transactional;
import com.alexistdev.geobill.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class PermissionService {

    private final RoleMenuRepo roleMenuRepo;
    private final MenuRepo menuRepo;
    private final MessagesUtils messagesUtils;

    public PermissionService(RoleMenuRepo roleMenuRepo, MenuRepo menuRepo, MessagesUtils messagesUtils) {
        this.roleMenuRepo = roleMenuRepo;
        this.menuRepo = menuRepo;
        this.messagesUtils = messagesUtils;
    }

    public RoleMenu addPermission(Role role, UUID menuId) {
        Menu foundMenu = menuRepo.findById(menuId)
                .orElseThrow(() -> new RuntimeException(messagesUtils.getMessage("permissionservice.menu_not_found", menuId.toString())));

        if (Role.ADMIN.equals(role)) {
            throw new RuntimeException(messagesUtils.getMessage("permissionservice.role_not_found"));
        }

        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRole(role);
        roleMenu.setMenu(foundMenu);
        return roleMenuRepo.save(roleMenu);
    }

}
