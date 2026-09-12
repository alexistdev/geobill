package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.TicketDepartment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketDepartmentRepo extends JpaRepository<TicketDepartment, UUID> {

    Optional<TicketDepartment> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    /** Isi dropdown Department; departemen non-aktif tidak boleh dipilih untuk tiket baru. */
    List<TicketDepartment> findByIsActiveTrueOrderBySortOrderAsc();

    @Query("SELECT d FROM TicketDepartment d WHERE d.isDeleted = false ORDER BY d.sortOrder ASC")
    Page<TicketDepartment> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT d FROM TicketDepartment d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND d.isDeleted = false")
    Page<TicketDepartment> findByFilter(@Param("keyword") String keyword, Pageable pageable);
}
