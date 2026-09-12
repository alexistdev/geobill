package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.TicketReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketReplyRepo extends JpaRepository<TicketReply, UUID> {

    /** Percakapan lengkap untuk staff, termasuk catatan internal. */
    @Query("SELECT r FROM TicketReply r LEFT JOIN FETCH r.user u WHERE r.ticket.id = :ticketId AND r.isDeleted = false ORDER BY r.createdDate ASC")
    List<TicketReply> findByTicketId(@Param("ticketId") UUID ticketId);

    /** Percakapan versi klien; catatan internal tidak pernah ikut. */
    @Query("SELECT r FROM TicketReply r LEFT JOIN FETCH r.user u WHERE r.ticket.id = :ticketId AND r.isInternalNote = false AND r.isDeleted = false ORDER BY r.createdDate ASC")
    List<TicketReply> findPublicByTicketId(@Param("ticketId") UUID ticketId);

    Page<TicketReply> findByTicket_IdOrderByCreatedDateDesc(UUID ticketId, Pageable pageable);

    /** Pesan pembuka tiket. */
    Optional<TicketReply> findFirstByTicket_IdOrderByCreatedDateAsc(UUID ticketId);

    Optional<TicketReply> findFirstByTicket_IdAndIsInternalNoteFalseOrderByCreatedDateDesc(UUID ticketId);

    long countByTicket_IdAndIsInternalNoteFalse(UUID ticketId);

    long countByTicket_IdAndIsInternalNoteTrue(UUID ticketId);

    /** Antrean notifikasi email yang belum terkirim. */
    List<TicketReply> findByIsEmailSentFalseAndIsInternalNoteFalse();
}
