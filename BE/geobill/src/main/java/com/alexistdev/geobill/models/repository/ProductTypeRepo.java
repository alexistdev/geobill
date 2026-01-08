package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductTypeRepo extends JpaRepository<ProductType, UUID> {

    Page<ProductType> findByIsDeletedFalse(Pageable pageable);

    @Query(value = "SELECT * FROM tb_product_types WHERE name = :name", nativeQuery = true)
    Optional<ProductType> findByNameIncludingDeleted(@Param("name") String name);
}
