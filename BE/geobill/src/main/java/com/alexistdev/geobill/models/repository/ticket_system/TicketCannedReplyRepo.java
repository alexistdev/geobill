package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.TicketCannedReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TicketCannedReplyRepo extends JpaRepository<TicketCannedReply, UUID> {

    List<TicketCannedReply> findByIsActiveTrueOrderBySortOrderAsc();

    boolean existsByTitle(String title);

    /** Template milik satu departemen digabung dengan template global. */
    @Query("SELECT c FROM TicketCannedReply c WHERE (c.department.id = :departmentId OR c.department IS NULL) AND c.isActive = true AND c.isDeleted = false ORDER BY c.sortOrder ASC")
    List<TicketCannedReply> findAvailableForDepartment(@Param("departmentId") UUID departmentId);

    @Query("SELECT c FROM TicketCannedReply c LEFT JOIN FETCH c.department d WHERE c.isDeleted = false ORDER BY c.sortOrder ASC")
    Page<TicketCannedReply> findByIsDeletedFalse(Pageable pageable);
}
