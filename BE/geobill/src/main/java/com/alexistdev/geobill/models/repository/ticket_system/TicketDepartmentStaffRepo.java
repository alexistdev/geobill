package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.TicketDepartmentStaff;
import com.alexistdev.geobill.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketDepartmentStaffRepo extends JpaRepository<TicketDepartmentStaff, UUID> {

    boolean existsByDepartment_IdAndUser_Id(UUID departmentId, UUID userId);

    Optional<TicketDepartmentStaff> findByDepartment_IdAndUser_Id(UUID departmentId, UUID userId);

    List<TicketDepartmentStaff> findByDepartment_IdOrderByUser_FullNameAsc(UUID departmentId);

    List<TicketDepartmentStaff> findByUser_Id(UUID userId);

    /** Isi dropdown Assigned To untuk satu departemen. */
    @Query("SELECT s.user FROM TicketDepartmentStaff s WHERE s.department.id = :departmentId AND s.isDeleted = false ORDER BY s.user.fullName ASC")
    List<User> findStaffByDepartmentId(@Param("departmentId") UUID departmentId);

    /** Penerima eskalasi bila tiket melewati batas waktu. */
    @Query("SELECT s.user FROM TicketDepartmentStaff s WHERE s.department.id = :departmentId AND s.isSupervisor = true AND s.isDeleted = false")
    List<User> findSupervisorsByDepartmentId(@Param("departmentId") UUID departmentId);
}
