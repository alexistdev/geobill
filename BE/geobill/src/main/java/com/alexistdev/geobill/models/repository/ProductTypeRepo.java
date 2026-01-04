package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductTypeRepo extends JpaRepository<ProductType, UUID> {
}
