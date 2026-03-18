package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.HostingDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.request.HostingRequest;
import com.alexistdev.geobill.services.HostingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ResponseData<HostingDTO>> addHosting(@Valid @RequestBody HostingRequest request){
        HostingDTO hostingDTO = hostingService.addHosting(request);

        ResponseData<HostingDTO> responseData = new ResponseData<>();
        responseData.setStatus(false);
        responseData.setPayload(null);
        responseData.setStatus(true);
        responseData.getMessages().add("Hosting added successfully");
        responseData.setPayload(hostingDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

}
