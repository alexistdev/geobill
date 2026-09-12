package com.alexistdev.geobill.services.ticket_system;

import com.alexistdev.geobill.dto.ticket_system.TicketAttachmentDTO;
import com.alexistdev.geobill.exceptions.ConflictException;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.Ticket;
import com.alexistdev.geobill.models.entity.TicketAttachment;
import com.alexistdev.geobill.models.entity.TicketReply;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.ticket_system.TicketAttachmentRepo;
import com.alexistdev.geobill.models.repository.ticket_system.TicketReplyRepo;
import com.alexistdev.geobill.models.repository.ticket_system.TicketRepo;
import com.alexistdev.geobill.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

/**
 * Mencatat metadata lampiran. Penyimpanan filenya sendiri berada di luar service ini:
 * yang disimpan hanya lokasi file, sehingga disk lokal maupun object storage sama saja.
 */
@Slf4j
@Service
public class TicketAttachmentService {

    private static final String DATE_PATTERN = "dd-MM-yyyy HH:mm";
    private static final long DEFAULT_QUOTA_BYTES = 20L * 1024 * 1024;

    private final TicketAttachmentRepo attachmentRepo;
    private final TicketReplyRepo replyRepo;
    private final TicketRepo ticketRepo;
    private final MessagesUtils messagesUtils;
    private final long quotaPerTicket;

    public TicketAttachmentService(TicketAttachmentRepo attachmentRepo,
                                   TicketReplyRepo replyRepo,
                                   TicketRepo ticketRepo,
                                   MessagesUtils messagesUtils,
                                   @Value("${geobill.ticket.attachment.quota-bytes:" + DEFAULT_QUOTA_BYTES + "}")
                                   long quotaPerTicket) {
        this.attachmentRepo = attachmentRepo;
        this.replyRepo = replyRepo;
        this.ticketRepo = ticketRepo;
        this.messagesUtils = messagesUtils;
        this.quotaPerTicket = quotaPerTicket;
    }

    public List<TicketAttachmentDTO> getAttachmentsByTicket(UUID ticketId) {
        return attachmentRepo.findByTicket_IdOrderByCreatedDateAsc(ticketId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<TicketAttachmentDTO> getAttachmentsByReply(UUID replyId) {
        return attachmentRepo.findByReply_Id(replyId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public TicketAttachment findEntityById(UUID id) {
        return attachmentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("ticketattachmentservice.attachment_not_found", id.toString())));
    }

    public TicketAttachment findByStoredName(String storedName) {
        return attachmentRepo.findByStoredName(storedName)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("ticketattachmentservice.attachment_not_found", storedName)));
    }

    /**
     * @param replyId balasan tempat lampiran menempel, boleh null bila menempel langsung pada tiket.
     */
    @Transactional
    public TicketAttachmentDTO addAttachment(UUID ticketId, UUID replyId, String originalName, String storedName,
                                             String storagePath, String mimeType, long fileSize, User uploadedBy) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("ticketattachmentservice.ticket_not_found")));

        TicketReply reply = null;
        if (replyId != null) {
            reply = replyRepo.findById(replyId)
                    .orElseThrow(() -> new NotFoundException(
                            messagesUtils.getMessage("ticketattachmentservice.reply_not_found")));
            if (!reply.getTicket().getId().equals(ticketId)) {
                throw new ConflictException(messagesUtils.getMessage("ticketattachmentservice.reply_other_ticket"));
            }
        }

        long used = attachmentRepo.sumFileSizeByTicketId(ticketId);
        if (used + fileSize > quotaPerTicket) {
            throw new ConflictException(messagesUtils.getMessage("ticketattachmentservice.quota_exceeded",
                    quotaPerTicket + " bytes"));
        }

        TicketAttachment attachment = new TicketAttachment();
        attachment.setTicket(ticket);
        attachment.setReply(reply);
        attachment.setOriginalName(originalName);
        attachment.setStoredName(storedName);
        attachment.setStoragePath(storagePath);
        attachment.setMimeType(mimeType);
        attachment.setFileSize(fileSize);
        attachment.setUploadedBy(uploadedBy);
        return convertToDTO(attachmentRepo.save(attachment));
    }

    /** Hanya menghapus catatannya; file di penyimpanan dibersihkan pemanggilnya. */
    @Transactional
    public void deleteAttachment(UUID id) {
        attachmentRepo.delete(findEntityById(id));
    }

    public long getUsedQuota(UUID ticketId) {
        return attachmentRepo.sumFileSizeByTicketId(ticketId);
    }

    public long getQuotaPerTicket() {
        return quotaPerTicket;
    }

    private TicketAttachmentDTO convertToDTO(TicketAttachment attachment) {
        TicketAttachmentDTO dto = new TicketAttachmentDTO();
        dto.setId(attachment.getId().toString());
        dto.setTicketId(attachment.getTicket().getId().toString());
        dto.setReplyId(attachment.getReply() == null ? null : attachment.getReply().getId().toString());
        dto.setName(attachment.getOriginalName());
        dto.setMimeType(attachment.getMimeType());
        dto.setFileSize(attachment.getFileSize());
        dto.setUploadedBy(attachment.getUploadedBy() == null ? null : attachment.getUploadedBy().getFullName());
        dto.setCreatedDate(attachment.getCreatedDate() == null
                ? null
                : new SimpleDateFormat(DATE_PATTERN).format(attachment.getCreatedDate()));
        return dto;
    }
}
