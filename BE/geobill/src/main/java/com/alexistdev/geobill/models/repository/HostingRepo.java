package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface HostingRepo extends JpaRepository<Hosting, UUID> {
    boolean existsByHostingCode(String hostingCode);
    boolean existsByUser_IdAndStatus(UUID userId, int status);

    @Query("SELECT h FROM Hosting h LEFT JOIN FETCH h.product pt WHERE h.isDeleted = false AND pt.isDeleted = false AND h.user = :user")
    Page<Hosting> findByUserAndIsDeletedFalse(Pageable pageable, User user);

    @Query("SELECT h FROM Hosting h LEFT JOIN FETCH h.product pt WHERE h.name LIKE %:keyword% AND h.isDeleted = false AND pt.isDeleted = false AND h.user = :user")
    Page<Hosting> findByUserWithFilterAndIsDeletedFalse(@Param("keyword") String keyword, @Param("user") User user, Pageable pageable);

}
