package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Domaintld;
import com.alexistdev.geobill.models.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuRepo extends JpaRepository<Menu, UUID> {
}
