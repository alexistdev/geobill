package com.alexistdev.geobill.services.ticket_system;

import com.alexistdev.geobill.dto.ticket_system.TicketCannedReplyDTO;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.TicketCannedReply;
import com.alexistdev.geobill.models.entity.TicketDepartment;
import com.alexistdev.geobill.models.repository.ticket_system.TicketCannedReplyRepo;
import com.alexistdev.geobill.models.repository.ticket_system.TicketDepartmentRepo;
import com.alexistdev.geobill.request.ticket_system.TicketCannedReplyRequest;
import com.alexistdev.geobill.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TicketCannedReplyService {

    private final TicketCannedReplyRepo cannedReplyRepo;
    private final TicketDepartmentRepo departmentRepo;
    private final MessagesUtils messagesUtils;

    public TicketCannedReplyService(TicketCannedReplyRepo cannedReplyRepo,
                                    TicketDepartmentRepo departmentRepo,
                                    MessagesUtils messagesUtils) {
        this.cannedReplyRepo = cannedReplyRepo;
        this.departmentRepo = departmentRepo;
        this.messagesUtils = messagesUtils;
    }

    public Page<TicketCannedReplyDTO> getAllCannedReplies(Pageable pageable) {
        Page<TicketCannedReply> result = cannedReplyRepo.findByIsDeletedFalse(pageable);
        List<TicketCannedReplyDTO> content = result.getContent().stream()
                .map(this::convertToDTO)
                .toList();
        return new PageImpl<>(content, pageable, result.getTotalElements());
    }

    /** Template milik departemen tersebut digabung dengan template global. */
    public List<TicketCannedReplyDTO> getAvailableForDepartment(UUID departmentId) {
        return cannedReplyRepo.findAvailableForDepartment(departmentId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<TicketCannedReplyDTO> getActiveCannedReplies() {
        return cannedReplyRepo.findByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public TicketCannedReply findEntityById(UUID id) {
        return cannedReplyRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("ticketcannedreplyservice.canned_reply_not_found", id.toString())));
    }

    public TicketCannedReplyDTO getCannedReplyById(UUID id) {
        return convertToDTO(findEntityById(id));
    }

    @Transactional
    public TicketCannedReplyDTO addCannedReply(TicketCannedReplyRequest request) {
        if (cannedReplyRepo.existsByTitle(request.getTitle())) {
            throw new DuplicateException(
                    messagesUtils.getMessage("ticketcannedreplyservice.title_exist", request.getTitle()));
        }

        TicketCannedReply cannedReply = new TicketCannedReply();
        applyRequest(cannedReply, request);
        return convertToDTO(cannedReplyRepo.save(cannedReply));
    }

    @Transactional
    public TicketCannedReplyDTO updateCannedReply(UUID id, TicketCannedReplyRequest request) {
        TicketCannedReply cannedReply = findEntityById(id);

        if (!cannedReply.getTitle().equals(request.getTitle()) && cannedReplyRepo.existsByTitle(request.getTitle())) {
            throw new DuplicateException(
                    messagesUtils.getMessage("ticketcannedreplyservice.title_exist", request.getTitle()));
        }

        applyRequest(cannedReply, request);
        return convertToDTO(cannedReplyRepo.save(cannedReply));
    }

    @Transactional
    public void deleteCannedReply(UUID id) {
        cannedReplyRepo.delete(findEntityById(id));
    }

    private void applyRequest(TicketCannedReply cannedReply, TicketCannedReplyRequest request) {
        cannedReply.setTitle(request.getTitle());
        cannedReply.setBody(request.getBody());
        cannedReply.setDepartment(findDepartment(request.getDepartmentId()));
        if (request.getSortOrder() != null) {
            cannedReply.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            cannedReply.setIsActive(request.getIsActive());
        }
    }

    /** Departemen kosong berarti template berlaku untuk semua departemen. */
    private TicketDepartment findDepartment(String departmentId) {
        if (departmentId == null || departmentId.isBlank()) {
            return null;
        }
        UUID id = UUID.fromString(departmentId);
        return departmentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("ticketdepartmentservice.department_not_found", departmentId)));
    }

    private TicketCannedReplyDTO convertToDTO(TicketCannedReply cannedReply) {
        TicketCannedReplyDTO dto = new TicketCannedReplyDTO();
        dto.setId(cannedReply.getId().toString());
        dto.setDepartmentId(cannedReply.getDepartment() == null
                ? null
                : cannedReply.getDepartment().getId().toString());
        dto.setDepartment(cannedReply.getDepartment() == null ? null : cannedReply.getDepartment().getName());
        dto.setTitle(cannedReply.getTitle());
        dto.setBody(cannedReply.getBody());
        dto.setActive(Boolean.TRUE.equals(cannedReply.getIsActive()));
        dto.setSortOrder(cannedReply.getSortOrder());
        return dto;
    }
}
