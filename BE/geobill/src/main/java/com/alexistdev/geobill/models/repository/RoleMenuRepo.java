package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RoleMenuRepo extends JpaRepository<RoleMenu, UUID> {
}
