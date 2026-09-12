package com.alexistdev.geobill.controllers.ticket_system;

import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.dto.ticket_system.TicketDTO;
import com.alexistdev.geobill.dto.ticket_system.TicketDetailDTO;
import com.alexistdev.geobill.dto.ticket_system.TicketReplyDTO;
import com.alexistdev.geobill.dto.ticket_system.TicketTabDTO;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.request.ticket_system.TicketFilterRequest;
import com.alexistdev.geobill.request.ticket_system.TicketReplyRequest;
import com.alexistdev.geobill.request.ticket_system.TicketRequest;
import com.alexistdev.geobill.services.ticket_system.TicketService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final MessagesUtils messagesUtils;

    public TicketController(TicketService ticketService, MessagesUtils messagesUtils) {
        this.ticketService = ticketService;
        this.messagesUtils = messagesUtils;
    }

    /* --------------------------------------------------------------- admin */

    @GetMapping
    public ResponseEntity<ResponseData<Page<TicketDTO>>> getTickets(
            @RequestParam(defaultValue = "awaiting_staff") String status,
            @RequestParam(required = false) String ticketNumber,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String lastReply,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "lastReplyAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        TicketFilterRequest filter = new TicketFilterRequest();
        filter.setTicketNumber(ticketNumber);
        filter.setSummary(summary);
        filter.setPriority(priority);
        filter.setDepartmentId(departmentId);
        filter.setAssignedTo(assignedTo);
        filter.setLastReply(lastReply);

        Page<TicketDTO> result = ticketService.getTickets(status, filter, pageable(page, size, sortBy, direction));
        return pagedResponse(result, page);
    }

    @GetMapping("/tabs")
    public ResponseEntity<ResponseData<List<TicketTabDTO>>> getTabs() {
        return listResponse(ticketService.getTabs(), messagesUtils.getMessage("ticketcontroller.tabs_retrieved"));
    }

    /** Antrean tiket yang sedang ditangani staff yang login. */
    @GetMapping("/assigned/me")
    public ResponseEntity<ResponseData<Page<TicketDTO>>> getMyQueue(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size) {

        Page<TicketDTO> result = ticketService.getOpenTicketsByStaff(currentUser.getId(), PageRequest.of(page, size));
        return pagedResponse(result, page);
    }

    /* ---------------------------------------------------------------- klien */

    @GetMapping("/me")
    public ResponseEntity<ResponseData<Page<TicketDTO>>> getMyTickets(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "lastReplyAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Page<TicketDTO> result = ticketService.getTicketsByUser(currentUser, status,
                pageable(page, size, sortBy, direction));
        return pagedResponse(result, page);
    }

    @GetMapping("/tabs/me")
    public ResponseEntity<ResponseData<List<TicketTabDTO>>> getMyTabs(@AuthenticationPrincipal User currentUser) {
        return listResponse(ticketService.getTabsByUser(currentUser), messagesUtils.getMessage("ticketcontroller.tabs_retrieved"));
    }

    /* ------------------------------------------------------------ keduanya */

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<TicketDetailDTO>> getTicketDetail(@PathVariable("id") UUID id,
                                                                        @AuthenticationPrincipal User currentUser) {
        return okResponse(ticketService.getTicketDetail(id, currentUser), messagesUtils.getMessage("ticketcontroller.ticket_found"));
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<ResponseData<TicketDetailDTO>> getTicketByNumber(
            @PathVariable("ticketNumber") String ticketNumber,
            @AuthenticationPrincipal User currentUser) {
        return okResponse(ticketService.getTicketByNumber(ticketNumber, currentUser),
                messagesUtils.getMessage("ticketcontroller.ticket_found"));
    }

    /**
     * Pemilik tiket diambil dari pengguna yang login. Klien tidak bisa membuka tiket atas nama
     * orang lain walau mengirim userId di body, dan hanya staff atau admin yang boleh melakukannya.
     */
    @PostMapping
    public ResponseEntity<ResponseData<TicketDetailDTO>> createTicket(@Valid @RequestBody TicketRequest request,
                                                                     @AuthenticationPrincipal User currentUser) {
        request.setUserId(resolveActingUserId(request.getUserId(), currentUser));
        TicketDetailDTO result = ticketService.createTicket(request);

        ResponseData<TicketDetailDTO> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(messagesUtils.getMessage("ticketcontroller.ticket_created"));
        responseData.setPayload(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<ResponseData<TicketReplyDTO>> addReply(@PathVariable("id") UUID id,
                                                                @Valid @RequestBody TicketReplyRequest request,
                                                                @AuthenticationPrincipal User currentUser) {
        request.setTicketId(id.toString());
        request.setUserId(resolveActingUserId(request.getUserId(), currentUser));
        TicketReplyDTO result = ticketService.addReply(request);

        ResponseData<TicketReplyDTO> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(messagesUtils.getMessage("ticketcontroller.reply_created"));
        responseData.setPayload(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    /* ------------------------------------------------------------ penanganan */

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ResponseData<TicketDetailDTO>> assignTicket(@PathVariable("id") UUID id,
                                                                     @RequestParam UUID staffId,
                                                                     @AuthenticationPrincipal User currentUser) {
        return okResponse(ticketService.assignTicket(id, staffId, currentUser),
                messagesUtils.getMessage("ticketcontroller.ticket_assigned"));
    }

    @PatchMapping("/{id}/unassign")
    public ResponseEntity<ResponseData<TicketDetailDTO>> unassignTicket(@PathVariable("id") UUID id,
                                                                       @AuthenticationPrincipal User currentUser) {
        return okResponse(ticketService.unassignTicket(id, currentUser),
                messagesUtils.getMessage("ticketcontroller.ticket_unassigned"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ResponseData<TicketDetailDTO>> changeStatus(@PathVariable("id") UUID id,
                                                                     @RequestParam String status,
                                                                     @AuthenticationPrincipal User currentUser) {
        return okResponse(ticketService.changeStatus(id, status, currentUser),
                messagesUtils.getMessage("ticketcontroller.status_updated"));
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<ResponseData<TicketDetailDTO>> changePriority(@PathVariable("id") UUID id,
                                                                       @RequestParam String priority,
                                                                       @AuthenticationPrincipal User currentUser) {
        return okResponse(ticketService.changePriority(id, priority, currentUser),
                messagesUtils.getMessage("ticketcontroller.priority_updated"));
    }

    @PatchMapping("/{id}/department")
    public ResponseEntity<ResponseData<TicketDetailDTO>> changeDepartment(@PathVariable("id") UUID id,
                                                                         @RequestParam UUID departmentId,
                                                                         @AuthenticationPrincipal User currentUser) {
        return okResponse(ticketService.changeDepartment(id, departmentId, currentUser),
                messagesUtils.getMessage("ticketcontroller.department_updated"));
    }

    /* ------------------------------------------------------------ trash */

    @PatchMapping("/{id}/trash")
    public ResponseEntity<ResponseData<TicketDetailDTO>> moveToTrash(@PathVariable("id") UUID id,
                                                                    @AuthenticationPrincipal User currentUser) {
        return okResponse(ticketService.moveToTrash(id, currentUser), messagesUtils.getMessage("ticketcontroller.ticket_trashed"));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ResponseData<TicketDetailDTO>> restoreFromTrash(@PathVariable("id") UUID id,
                                                                         @AuthenticationPrincipal User currentUser) {
        return okResponse(ticketService.restoreFromTrash(id, currentUser),
                messagesUtils.getMessage("ticketcontroller.ticket_restored"));
    }

    /** Hanya untuk tiket yang sudah ada di Trash. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteTicket(@PathVariable("id") UUID id) {
        ticketService.deleteTicket(id);

        ResponseData<Void> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(messagesUtils.getMessage("ticketcontroller.ticket_deleted"));
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping("/auto-close")
    public ResponseEntity<ResponseData<Integer>> autoCloseIdleTickets() {
        int closed = ticketService.autoCloseIdleTickets();
        return okResponse(closed, messagesUtils.getMessage("ticketcontroller.auto_closed", String.valueOf(closed)));
    }

    /* ------------------------------------------------------------ bantuan */

    /**
     * Klien selalu bertindak sebagai dirinya sendiri. Hanya staff dan admin yang boleh
     * mengirimkan userId lain, misalnya saat membuka tiket atas nama klien lewat telepon.
     */
    private String resolveActingUserId(String requestedUserId, User currentUser) {
        boolean actorIsStaff = currentUser.getRole() == Role.STAFF || currentUser.getRole() == Role.ADMIN;
        if (actorIsStaff && requestedUserId != null && !requestedUserId.isBlank()) {
            return requestedUserId;
        }
        return currentUser.getId().toString();
    }

    private Pageable pageable(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }

    private ResponseEntity<ResponseData<Page<TicketDTO>>> pagedResponse(Page<TicketDTO> result, int page) {
        ResponseData<Page<TicketDTO>> responseData = new ResponseData<>();
        responseData.setStatus(!result.isEmpty());
        responseData.getMessages().add(result.isEmpty()
                ? messagesUtils.getMessage("ticketcontroller.no_ticket")
                : messagesUtils.getMessage("ticketcontroller.page_retrieved", String.valueOf(page)));
        responseData.setPayload(result);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    private <T> ResponseEntity<ResponseData<List<T>>> listResponse(List<T> result, String message) {
        ResponseData<List<T>> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(message);
        responseData.setPayload(result);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    private <T> ResponseEntity<ResponseData<T>> okResponse(T payload, String message) {
        ResponseData<T> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(message);
        responseData.setPayload(payload);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }
}
