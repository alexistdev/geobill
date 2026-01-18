package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepo extends JpaRepository<Product, UUID> {

    Page<Product> findByIsDeletedFalse(Pageable pageable);

    @Query(value = "SELECT * FROM tb_products WHERE name = :name", nativeQuery = true)
    Optional<Product> findByNameIncludingDeleted(@Param("name") String name);

    @Query("SELECT pt FROM Product pt WHERE pt.name LIKE %:keyword% and pt.isDeleted=false")
    Page<Product> findByFilter(@Param("keyword") String keyword, Pageable pageable);
}
