package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketAttachmentRepo extends JpaRepository<TicketAttachment, UUID> {

    List<TicketAttachment> findByTicket_IdOrderByCreatedDateAsc(UUID ticketId);

    List<TicketAttachment> findByReply_Id(UUID replyId);

    /** Nama simpan dipakai sebagai kunci saat file diunduh kembali. */
    Optional<TicketAttachment> findByStoredName(String storedName);

    long countByTicket_Id(UUID ticketId);

    /** Total byte yang terpakai satu tiket, untuk pembatasan kuota lampiran. */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM TicketAttachment a WHERE a.ticket.id = :ticketId AND a.isDeleted = false")
    Long sumFileSizeByTicketId(@Param("ticketId") UUID ticketId);
}
