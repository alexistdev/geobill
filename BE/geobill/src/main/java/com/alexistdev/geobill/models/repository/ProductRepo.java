package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepo extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productType pt WHERE p.isDeleted = false AND pt.isDeleted = false")
    Page<Product> findByIsDeletedFalse(Pageable pageable);

    @Query(value = "SELECT * FROM tb_products WHERE name = :name", nativeQuery = true)
    Optional<Product> findByNameIncludingDeleted(@Param("name") String name);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productType pt WHERE p.name LIKE %:keyword% AND p.isDeleted = false AND pt.isDeleted = false")
    Page<Product> findByFilter(@Param("keyword") String keyword, Pageable pageable);
}
