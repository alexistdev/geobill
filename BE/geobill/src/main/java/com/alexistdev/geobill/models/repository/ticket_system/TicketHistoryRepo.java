package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.TicketHistory;
import com.alexistdev.geobill.models.entity.TicketHistoryAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketHistoryRepo extends JpaRepository<TicketHistory, UUID> {

    @Query("SELECT h FROM TicketHistory h LEFT JOIN FETCH h.actor a WHERE h.ticket.id = :ticketId AND h.isDeleted = false ORDER BY h.createdDate DESC")
    List<TicketHistory> findByTicketId(@Param("ticketId") UUID ticketId);

    Page<TicketHistory> findByTicket_IdOrderByCreatedDateDesc(UUID ticketId, Pageable pageable);

    /**
     * Catatan terakhir untuk satu jenis aksi. Restore dari Trash membaca entri TRASHED
     * terakhir untuk mengetahui status tiket sebelum dibuang.
     */
    Optional<TicketHistory> findFirstByTicket_IdAndActionOrderByCreatedDateDesc(UUID ticketId,
                                                                               TicketHistoryAction action);

    List<TicketHistory> findByTicket_IdAndAction(UUID ticketId, TicketHistoryAction action);

    long countByTicket_Id(UUID ticketId);
}
