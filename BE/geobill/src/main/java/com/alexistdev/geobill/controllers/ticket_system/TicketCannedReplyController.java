package com.alexistdev.geobill.controllers.ticket_system;

import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.dto.ticket_system.TicketCannedReplyDTO;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.request.ticket_system.TicketCannedReplyRequest;
import com.alexistdev.geobill.services.ticket_system.TicketCannedReplyService;
import com.alexistdev.geobill.utils.MessagesUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/ticket-canned-replies")
public class TicketCannedReplyController {

    private final TicketCannedReplyService cannedReplyService;
    private final MessagesUtils messagesUtils;

    public TicketCannedReplyController(TicketCannedReplyService cannedReplyService, MessagesUtils messagesUtils) {
        this.cannedReplyService = cannedReplyService;
        this.messagesUtils = messagesUtils;
    }

    @GetMapping
    public ResponseEntity<ResponseData<Page<TicketCannedReplyDTO>>> getAllCannedReplies(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<TicketCannedReplyDTO> result = cannedReplyService.getAllCannedReplies(pageable);

        ResponseData<Page<TicketCannedReplyDTO>> responseData = new ResponseData<>();
        responseData.setStatus(!result.isEmpty());
        responseData.getMessages().add(result.isEmpty()
                ? messagesUtils.getMessage("ticketcannedreplycontroller.no_canned_reply")
                : messagesUtils.getMessage("ticketcannedreplycontroller.page_retrieved", String.valueOf(page)));
        responseData.setPayload(result);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @GetMapping("/active")
    public ResponseEntity<ResponseData<List<TicketCannedReplyDTO>>> getActiveCannedReplies() {
        return listResponse(cannedReplyService.getActiveCannedReplies(),
                messagesUtils.getMessage("ticketcannedreplycontroller.active_retrieved"));
    }

    /** Template milik departemen tersebut digabung dengan template global. */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ResponseData<List<TicketCannedReplyDTO>>> getAvailableForDepartment(
            @PathVariable("departmentId") UUID departmentId) {
        return listResponse(cannedReplyService.getAvailableForDepartment(departmentId),
                messagesUtils.getMessage("ticketcannedreplycontroller.department_retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<TicketCannedReplyDTO>> getCannedReplyById(@PathVariable("id") UUID id) {
        return okResponse(cannedReplyService.getCannedReplyById(id),
                messagesUtils.getMessage("ticketcannedreplycontroller.canned_reply_found"), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseData<TicketCannedReplyDTO>> addCannedReply(
            @Valid @RequestBody TicketCannedReplyRequest request) {
        try {
            return okResponse(cannedReplyService.addCannedReply(request),
                    messagesUtils.getMessage("ticketcannedreplycontroller.canned_reply_created"), HttpStatus.CREATED);
        } catch (DuplicateException d) {
            log.error("Error creating canned reply", d);
            return conflictResponse(d.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseData<TicketCannedReplyDTO>> updateCannedReply(
            @PathVariable("id") UUID id,
            @Valid @RequestBody TicketCannedReplyRequest request) {
        try {
            return okResponse(cannedReplyService.updateCannedReply(id, request),
                    messagesUtils.getMessage("ticketcannedreplycontroller.canned_reply_updated"), HttpStatus.OK);
        } catch (DuplicateException d) {
            log.error("Error updating canned reply", d);
            return conflictResponse(d.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteCannedReply(@PathVariable("id") UUID id) {
        cannedReplyService.deleteCannedReply(id);

        ResponseData<Void> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(messagesUtils.getMessage("ticketcannedreplycontroller.canned_reply_deleted"));
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    private ResponseEntity<ResponseData<List<TicketCannedReplyDTO>>> listResponse(List<TicketCannedReplyDTO> result,
                                                                                 String message) {
        ResponseData<List<TicketCannedReplyDTO>> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(message);
        responseData.setPayload(result);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    private ResponseEntity<ResponseData<TicketCannedReplyDTO>> okResponse(TicketCannedReplyDTO payload, String message,
                                                                         HttpStatus httpStatus) {
        ResponseData<TicketCannedReplyDTO> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(message);
        responseData.setPayload(payload);
        return ResponseEntity.status(httpStatus).body(responseData);
    }

    private ResponseEntity<ResponseData<TicketCannedReplyDTO>> conflictResponse(String message) {
        ResponseData<TicketCannedReplyDTO> responseData = new ResponseData<>();
        responseData.setStatus(false);
        responseData.getMessages().add(message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseData);
    }
}
