package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Domaintld;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DomaintldRepo extends JpaRepository<Domaintld, UUID> {
}
