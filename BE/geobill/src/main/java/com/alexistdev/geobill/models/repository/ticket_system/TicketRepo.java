package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.Ticket;
import com.alexistdev.geobill.models.entity.TicketPriority;
import com.alexistdev.geobill.models.entity.TicketStatus;
import com.alexistdev.geobill.models.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepo extends JpaRepository<Ticket, UUID> {

    boolean existsByTicketNumber(String ticketNumber);

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    /**
     * Nomor tiket unik di level database termasuk baris yang sudah di-soft-delete,
     * jadi pengecekannya harus melewati filter is_deleted.
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM tb_tickets WHERE ticket_number = :ticketNumber", nativeQuery = true)
    boolean existsByTicketNumberIncludingDeleted(@Param("ticketNumber") String ticketNumber);

    @Query(value = "SELECT COUNT(*) FROM tb_tickets WHERE ticket_number LIKE CONCAT(:prefix, '%')", nativeQuery = true)
    long countByTicketNumberPrefixIncludingDeleted(@Param("prefix") String prefix);

    /** Klien hanya boleh membuka tiket miliknya sendiri. */
    Optional<Ticket> findByIdAndUser(UUID id, User user);

    long countByStatus(TicketStatus status);

    long countByDepartment_Id(UUID departmentId);

    long countByAssignedTo_IdAndStatus(UUID assignedToId, TicketStatus status);

    /**
     * Angka pada setiap tab halaman ticket, satu query untuk semua status.
     * Status tanpa tiket tidak ikut terbawa, jadi pemanggilnya harus memakai nol sebagai default.
     */
    @Query("SELECT t.status AS status, COUNT(t) AS total FROM Ticket t WHERE t.isDeleted = false GROUP BY t.status")
    List<TicketStatusCount> countGroupByStatus();

    @Query("SELECT t.status AS status, COUNT(t) AS total FROM Ticket t WHERE t.user = :user AND t.isDeleted = false GROUP BY t.status")
    List<TicketStatusCount> countGroupByStatusForUser(@Param("user") User user);

    /**
     * Daftar tiket pada satu tab beserta seluruh filter di panel kiri. Setiap filter yang
     * dikirim null diabaikan, sehingga satu query melayani semua kombinasi.
     */
    @Query(value = """
            SELECT t FROM Ticket t
            LEFT JOIN FETCH t.user u
            LEFT JOIN FETCH t.department d
            LEFT JOIN FETCH t.assignedTo a
            WHERE t.status = :status
              AND t.isDeleted = false
              AND (:ticketNumber IS NULL OR LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :ticketNumber, '%')))
              AND (:subject IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :subject, '%')))
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:departmentId IS NULL OR d.id = :departmentId)
              AND (:assignedToId IS NULL OR a.id = :assignedToId)
              AND (:lastReplyFrom IS NULL OR t.lastReplyAt >= :lastReplyFrom)
            """,
            countQuery = """
            SELECT COUNT(t) FROM Ticket t
            WHERE t.status = :status
              AND t.isDeleted = false
              AND (:ticketNumber IS NULL OR LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :ticketNumber, '%')))
              AND (:subject IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :subject, '%')))
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:departmentId IS NULL OR t.department.id = :departmentId)
              AND (:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)
              AND (:lastReplyFrom IS NULL OR t.lastReplyAt >= :lastReplyFrom)
            """)
    Page<Ticket> findByFilter(@Param("status") TicketStatus status,
                              @Param("ticketNumber") String ticketNumber,
                              @Param("subject") String subject,
                              @Param("priority") TicketPriority priority,
                              @Param("departmentId") UUID departmentId,
                              @Param("assignedToId") UUID assignedToId,
                              @Param("lastReplyFrom") Date lastReplyFrom,
                              Pageable pageable);

    @Query(value = """
            SELECT t FROM Ticket t
            LEFT JOIN FETCH t.department d
            LEFT JOIN FETCH t.assignedTo a
            WHERE t.user = :user AND t.status = :status AND t.isDeleted = false
            """,
            countQuery = "SELECT COUNT(t) FROM Ticket t WHERE t.user = :user AND t.status = :status AND t.isDeleted = false")
    Page<Ticket> findByUserAndStatus(@Param("user") User user,
                                     @Param("status") TicketStatus status,
                                     Pageable pageable);

    @Query(value = """
            SELECT t FROM Ticket t
            LEFT JOIN FETCH t.department d
            LEFT JOIN FETCH t.assignedTo a
            WHERE t.user = :user AND t.isDeleted = false
            """,
            countQuery = "SELECT COUNT(t) FROM Ticket t WHERE t.user = :user AND t.isDeleted = false")
    Page<Ticket> findByUser(@Param("user") User user, Pageable pageable);

    /** Tiket yang perlu ditangani seorang staff, diurutkan dari yang paling lama tidak dibalas. */
    @Query(value = """
            SELECT t FROM Ticket t
            LEFT JOIN FETCH t.user u
            LEFT JOIN FETCH t.department d
            WHERE t.assignedTo.id = :assignedToId
              AND t.status NOT IN (com.alexistdev.geobill.models.entity.TicketStatus.CLOSED,
                                   com.alexistdev.geobill.models.entity.TicketStatus.TRASH)
              AND t.isDeleted = false
            ORDER BY t.lastReplyAt ASC
            """,
            countQuery = """
            SELECT COUNT(t) FROM Ticket t
            WHERE t.assignedTo.id = :assignedToId
              AND t.status NOT IN (com.alexistdev.geobill.models.entity.TicketStatus.CLOSED,
                                   com.alexistdev.geobill.models.entity.TicketStatus.TRASH)
              AND t.isDeleted = false
            """)
    Page<Ticket> findOpenByAssignedTo(@Param("assignedToId") UUID assignedToId, Pageable pageable);

    /**
     * Tiket yang menunggu klien dan sudah melewati batas diam, untuk penutupan otomatis
     * sesuai autoCloseDays pada departemennya.
     */
    @Query("""
            SELECT t FROM Ticket t
            WHERE t.status = com.alexistdev.geobill.models.entity.TicketStatus.AWAITING_CLIENT
              AND t.lastReplyAt < :idleSince
              AND t.isDeleted = false
            """)
    List<Ticket> findIdleAwaitingClient(@Param("idleSince") Date idleSince);
}
