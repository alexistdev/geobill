package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RoleMenuRepo extends JpaRepository<RoleMenu, UUID> {

    @Query("SELECT rm FROM RoleMenu rm JOIN FETCH rm.menu m LEFT JOIN FETCH m.parent LEFT JOIN FETCH m.children WHERE rm.role = :role")
    List<RoleMenu> findByRole(Role role);

}
