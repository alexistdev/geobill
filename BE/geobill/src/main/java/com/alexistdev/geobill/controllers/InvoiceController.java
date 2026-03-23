package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.InvoiceUserDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.services.InvoiceService;
import com.alexistdev.geobill.services.UserService;
import com.alexistdev.geobill.utils.MessagesUtils;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final UserService userService;
    private final MessagesUtils messagesUtils;

    public InvoiceController(InvoiceService invoiceService, UserService userService, MessagesUtils messagesUtils) {
        this.invoiceService = invoiceService;
        this.userService = userService;
        this.messagesUtils = messagesUtils;
    }

    @GetMapping("/{userId}/data")
    public ResponseEntity<ResponseData<Page<InvoiceUserDTO>>> getAllHostingsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        ResponseData<Page<InvoiceUserDTO>> responseData = new ResponseData<>();

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        User userFound = userService.findUserByUUID(UUID.fromString(userId));

        if (userFound == null) {
            String message = messagesUtils.getMessage("userservice.user.notfound", userId);
            throw new NotFoundException(message);
        }

        Page<InvoiceUserDTO> invoicePage;
        try {
            invoicePage = invoiceService.getAllInvoicesByUser(pageable, userFound);
        } catch (RuntimeException e){
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            invoicePage = invoiceService.getAllInvoicesByUser(fallbackPageable, userFound);
        }

        responseData.getMessages().add("No invoice found");
        responseData.setStatus(false);
        handleNonEmptyPage(responseData,invoicePage,page);
        responseData.setPayload(invoicePage);
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<InvoiceUserDTO>> getInvoiceByUUID(@PathVariable("id") String uuid) {
        ResponseData<InvoiceUserDTO> responseData = new ResponseData<>();
        responseData.setStatus(false);
        responseData.setPayload(null);
        InvoiceUserDTO result = invoiceService.getInvoiceById(uuid);
        responseData.getMessages().add("Retrieved invoice by id");
        responseData.setStatus(true);
        responseData.setPayload(result);
        return ResponseEntity.ok(responseData);
    }

    private <T> void handleNonEmptyPage(ResponseData<Page<T>> responseData, Page<?> pageResult, int pageNumber){
        if(!pageResult.isEmpty()){
            responseData.setStatus(true);
            if(!responseData.getMessages().isEmpty()){
                responseData.getMessages().removeFirst();
            }
            responseData.getMessages().add("Retrieved page " + pageNumber + " of products");
        }
    }

}
