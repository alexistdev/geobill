package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.HostingDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.request.HostingRequest;
import com.alexistdev.geobill.services.HostingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/hosting")
public class HostingController {

    private final HostingService hostingService;
    private final ModelMapper modelMapper;

    public HostingController(HostingService hostingService, ModelMapper modelMapper) {
        this.hostingService = hostingService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<ResponseData<HostingDTO>> addHosting(@Valid @RequestBody HostingRequest request, Errors errors){
        ResponseData<HostingDTO> responseData = new ResponseData<>();
        responseData.setStatus(false);
        responseData.setPayload(null);
        if(errors.hasErrors()) {
            processErrors(errors, responseData);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        try {
            Hosting hosting = hostingService.addHosting(request);

            HostingDTO hostingDTO = modelMapper.map(hosting, HostingDTO.class);
            hostingDTO.setUserId(UUID.fromString(request.getUserId()));
            hostingDTO.setProductId(UUID.fromString(request.getProductId()));

            responseData.setStatus(true);
            responseData.getMessages().add("Hosting added successfully");
            responseData.setPayload(hostingDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
        } catch (Exception e) {
            log.error("Error adding Hosting", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    private void processErrors(Errors errors, ResponseData<?> responseData) {
        for (ObjectError error : errors.getAllErrors()) {
            responseData.getMessages().add(error.getDefaultMessage());
        }
    }
}
