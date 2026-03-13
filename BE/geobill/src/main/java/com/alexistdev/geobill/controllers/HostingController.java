package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.HostingDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.request.HostingRequest;
import com.alexistdev.geobill.services.HostingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/hosting")
public class HostingController {

    private final HostingService hostingService;

    public HostingController(HostingService hostingService) {
        this.hostingService = hostingService;
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

            HostingDTO hostingDTO = this.mapToHostingDTO(hosting);

            responseData.setStatus(true);
            responseData.getMessages().add("Hosting added successfully");
            responseData.setPayload(hostingDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
        } catch (Exception e) {
            log.error("Error adding Hosting", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    private HostingDTO mapToHostingDTO(Hosting hosting) {
        HostingDTO hostingDTO = new HostingDTO();
        hostingDTO.setId(hosting.getId());
        hostingDTO.setUserId(hosting.getUser().getId());
        hostingDTO.setProductId(hosting.getProduct().getId());
        hostingDTO.setDomainName(hosting.getDomain());
        hostingDTO.setPrice(hosting.getPrice());
        hostingDTO.setCycle(hosting.getProduct().getCycle());
        return hostingDTO;
    }

    private void processErrors(Errors errors, ResponseData<?> responseData) {
        for (ObjectError error : errors.getAllErrors()) {
            responseData.getMessages().add(error.getDefaultMessage());
        }
    }
}
