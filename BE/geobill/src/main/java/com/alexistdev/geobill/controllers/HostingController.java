package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.HostingDTO;
import com.alexistdev.geobill.dto.HostingUserDTO;
import com.alexistdev.geobill.dto.ProductDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.request.HostingRequest;
import com.alexistdev.geobill.services.HostingService;
import com.alexistdev.geobill.services.UserService;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/hosting")
public class HostingController {

    private final HostingService hostingService;
    private final UserService userService;

    public HostingController(HostingService hostingService, UserService userService) {
        this.hostingService = hostingService;
        this.userService = userService;
    }

    @GetMapping("/{userId}/hostings")
    public ResponseEntity<ResponseData<Page<HostingUserDTO>>> getAllHostingsByUser(
            @PathVariable("userId") String userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        ResponseData<Page<HostingUserDTO>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        User userFound = userService.findUserByUUID(UUID.fromString(userId));

        if (userFound == null) {
            responseData.getMessages().add("User not found");
            responseData.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        Page<HostingUserDTO> hostingPage;
        try {
            hostingPage = hostingService.getAllHostingsByUser(pageable, userFound);
        } catch (RuntimeException e){
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            hostingPage = hostingService.getAllHostingsByUser(fallbackPageable, userFound);
        }

        responseData.getMessages().add("No hostings found");
        responseData.setStatus(false);
        handleNonEmptyPage(responseData,hostingPage,page);
        responseData.setPayload(hostingPage);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
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
