package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {


    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role !=:role")
    Page<User> findByRoleNot(@Param("role") Role role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:keyword% AND u.role != com.alexistdev.geobill.models.entity.Role.ADMIN AND u.isDeleted = false")
    Page<User> findByFilter(@Param("keyword") String keyword, Pageable pageable);
}
