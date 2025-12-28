package com.alexistdev.geobill.services;

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
import java.util.stream.Collectors;

@Slf4j
@Service
public class MenuService {

    @Autowired
    private MenuRepo menuRepo;

    @Autowired
    private RoleMenuRepo roleMenuRepo;

    @Transactional
    public List<Menu> getMenusByRole(Role role){
        List<Menu> menus = roleMenuRepo.findByRole(role).stream()
                .map(roleMenu -> roleMenu.getMenu())
                .collect(Collectors.toList());

        //force init of lazy loaded relationship (parent and children)
        for(Menu menu : menus){
            if(menu.getParent() != null) {
                menu.getParent().getName();
            }
            if(menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                menu.getChildren().size();
            }
        }
        return menus;
    }

    public Menu getMenuByName(String name) {
        return menuRepo.findByName(name);
    }
}
