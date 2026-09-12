package com.alexistdev.geobill.controllers.ticket_system;

import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.dto.ticket_system.TicketDepartmentDTO;
import com.alexistdev.geobill.dto.ticket_system.TicketStaffDTO;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.request.ticket_system.TicketDepartmentRequest;
import com.alexistdev.geobill.request.ticket_system.TicketStaffRequest;
import com.alexistdev.geobill.services.ticket_system.TicketDepartmentService;
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
@RequestMapping("/api/v1/ticket-departments")
public class TicketDepartmentController {

    private final TicketDepartmentService departmentService;
    private final MessagesUtils messagesUtils;

    public TicketDepartmentController(TicketDepartmentService departmentService, MessagesUtils messagesUtils) {
        this.departmentService = departmentService;
        this.messagesUtils = messagesUtils;
    }

    @GetMapping
    public ResponseEntity<ResponseData<Page<TicketDepartmentDTO>>> getAllDepartments(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<TicketDepartmentDTO> result = departmentService.getAllDepartments(pageable(page, size, sortBy, direction));
        return pagedResponse(result, page);
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<TicketDepartmentDTO>>> searchDepartments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<TicketDepartmentDTO> result = departmentService
                .getAllDepartmentsByFilter(pageable(page, size, sortBy, direction), keyword);
        return pagedResponse(result, page);
    }

    /** Isi dropdown Department saat klien membuka tiket baru. */
    @GetMapping("/active")
    public ResponseEntity<ResponseData<List<TicketDepartmentDTO>>> getActiveDepartments() {
        return listResponse(departmentService.getActiveDepartments(),
                messagesUtils.getMessage("ticketdepartmentcontroller.active_retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<TicketDepartmentDTO>> getDepartmentById(@PathVariable("id") UUID id) {
        return okResponse(departmentService.getDepartmentById(id),
                messagesUtils.getMessage("ticketdepartmentcontroller.department_found"), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseData<TicketDepartmentDTO>> addDepartment(
            @Valid @RequestBody TicketDepartmentRequest request) {
        try {
            return okResponse(departmentService.addDepartment(request),
                    messagesUtils.getMessage("ticketdepartmentcontroller.department_created"), HttpStatus.CREATED);
        } catch (DuplicateException d) {
            log.error("Error creating ticket department", d);
            return conflictResponse(d.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseData<TicketDepartmentDTO>> updateDepartment(
            @PathVariable("id") UUID id,
            @Valid @RequestBody TicketDepartmentRequest request) {
        try {
            return okResponse(departmentService.updateDepartment(id, request),
                    messagesUtils.getMessage("ticketdepartmentcontroller.department_updated"), HttpStatus.OK);
        } catch (DuplicateException d) {
            log.error("Error updating ticket department", d);
            return conflictResponse(d.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteDepartment(@PathVariable("id") UUID id) {
        departmentService.deleteDepartment(id);

        ResponseData<Void> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(messagesUtils.getMessage("ticketdepartmentcontroller.department_deleted"));
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    /* ------------------------------------------------------------ staff */

    /** Isi dropdown Assigned To untuk satu departemen. */
    @GetMapping("/{id}/staffs")
    public ResponseEntity<ResponseData<List<TicketStaffDTO>>> getStaffByDepartment(@PathVariable("id") UUID id) {
        return listResponse(departmentService.getStaffByDepartment(id),
                messagesUtils.getMessage("ticketdepartmentcontroller.staff_retrieved"));
    }

    @PostMapping("/staffs")
    public ResponseEntity<ResponseData<TicketStaffDTO>> assignStaff(@Valid @RequestBody TicketStaffRequest request) {
        ResponseData<TicketStaffDTO> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(messagesUtils.getMessage("ticketdepartmentcontroller.staff_assigned"));
        responseData.setPayload(departmentService.assignStaff(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @DeleteMapping("/{departmentId}/staffs/{userId}")
    public ResponseEntity<ResponseData<Void>> removeStaff(@PathVariable("departmentId") UUID departmentId,
                                                          @PathVariable("userId") UUID userId) {
        departmentService.removeStaff(departmentId, userId);

        ResponseData<Void> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(messagesUtils.getMessage("ticketdepartmentcontroller.staff_removed"));
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    /* ------------------------------------------------------------ bantuan */

    private Pageable pageable(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }

    private ResponseEntity<ResponseData<Page<TicketDepartmentDTO>>> pagedResponse(Page<TicketDepartmentDTO> result,
                                                                                 int page) {
        ResponseData<Page<TicketDepartmentDTO>> responseData = new ResponseData<>();
        responseData.setStatus(!result.isEmpty());
        responseData.getMessages().add(result.isEmpty()
                ? messagesUtils.getMessage("ticketdepartmentcontroller.no_department")
                : messagesUtils.getMessage("ticketdepartmentcontroller.page_retrieved", String.valueOf(page)));
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

    private ResponseEntity<ResponseData<TicketDepartmentDTO>> okResponse(TicketDepartmentDTO payload, String message,
                                                                        HttpStatus httpStatus) {
        ResponseData<TicketDepartmentDTO> responseData = new ResponseData<>();
        responseData.setStatus(true);
        responseData.getMessages().add(message);
        responseData.setPayload(payload);
        return ResponseEntity.status(httpStatus).body(responseData);
    }

    private ResponseEntity<ResponseData<TicketDepartmentDTO>> conflictResponse(String message) {
        ResponseData<TicketDepartmentDTO> responseData = new ResponseData<>();
        responseData.setStatus(false);
        responseData.getMessages().add(message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseData);
    }
}
