package com.alexistdev.geobill.controllers.ticket_system;

import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.dto.ticket_system.TicketCannedReplyDTO;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.request.ticket_system.TicketCannedReplyRequest;
import com.alexistdev.geobill.services.ticket_system.TicketCannedReplyService;
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

    private static final String NO_CANNED_REPLY_FOUND = "No canned replies found";

    private final TicketCannedReplyService cannedReplyService;

    public TicketCannedReplyController(TicketCannedReplyService cannedReplyService) {
        this.cannedReplyService = cannedReplyService;
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
                ? NO_CANNED_REPLY_FOUND
                : "Retrieved page " + page + " of canned replies");
        responseData.setPayload(result);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @GetMapping("/active")
    public ResponseEntity<ResponseData<List<TicketCannedReplyDTO>>> getActiveCannedReplies() {
        return listResponse(cannedReplyService.getActiveCannedReplies(), "Retrieved active canned replies");
    }

    /** Template milik departemen tersebut digabung dengan template global. */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ResponseData<List<TicketCannedReplyDTO>>> getAvailableForDepartment(
            @PathVariable("departmentId") UUID departmentId) {
        return listResponse(cannedReplyService.getAvailableForDepartment(departmentId),
                "Retrieved canned replies for department");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<TicketCannedReplyDTO>> getCannedReplyById(@PathVariable("id") UUID id) {
        return okResponse(cannedReplyService.getCannedReplyById(id), "Canned reply found", HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseData<TicketCannedReplyDTO>> addCannedReply(
            @Valid @RequestBody TicketCannedReplyRequest request) {
        try {
            return okResponse(cannedReplyService.addCannedReply(request),
                    "Canned reply successfully added", HttpStatus.CREATED);
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
                    "Canned reply successfully updated", HttpStatus.OK);
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
        responseData.getMessages().add("Canned reply has been deleted");
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
