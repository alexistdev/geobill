package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.dto.UserDTO;
import com.alexistdev.geobill.dto.UserDetailDTO;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.services.UserService;
import com.alexistdev.geobill.utils.MessagesUtils;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
@RequestMapping("/api/v1/users")
public class UserController {


    private final UserService userService;
    private final ModelMapper modelMapper;
    private final MessagesUtils messagesUtils;


    public UserController(UserService userService, ModelMapper modelMapper, MessagesUtils messagesUtils) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.messagesUtils = messagesUtils;
    }

    @GetMapping()
    public ResponseEntity<ResponseData<Page<UserDTO>>> getAllUserData(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        ResponseData<Page<UserDTO>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<User> usersPage;

        try {
            usersPage = userService.getAllUsers(pageable);
        } catch (RuntimeException e){
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            usersPage = userService.getAllUsers(fallbackPageable);
        }

        responseData.getMessages().add(this.messagesUtils.getMessage("usercontroller.user.nouser"));
        responseData.setStatus(false);

        handleNonEmptyPage(responseData, usersPage, page + 1);

        Page<UserDTO> userDTOS = usersPage
                .map(user -> modelMapper.map(user, UserDTO.class));
        responseData.setPayload(userDTOS);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<UserDTO>>> searchUser(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        ResponseData<Page<UserDTO>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<User> usersPage;
        try {
            usersPage = userService.getAllUsersByFilter(pageable, filter);
        } catch (RuntimeException e){
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            usersPage = userService.getAllUsersByFilter(fallbackPageable, filter);
        }
        responseData.getMessages().add(this.messagesUtils.getMessage("usercontroller.user.nouser"));
        responseData.setStatus(false);

        handleNonEmptyPage(responseData,usersPage,page);
        Page<UserDTO> userDTOS = usersPage
                .map(user -> modelMapper.map(user, UserDTO.class));
        responseData.setPayload(userDTOS);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<UserDetailDTO>> getUserDetail(@PathVariable("id") UUID uuid){
        ResponseData<UserDetailDTO> responseData = new ResponseData<>();
        responseData.setStatus(false);
        try {
            UserDetailDTO result = userService.getUserDetail(uuid);
            responseData.getMessages().add("Retrieved user detail");
            responseData.setStatus(true);
            responseData.setPayload(result);
            return ResponseEntity.status(HttpStatus.OK).body(responseData);
        } catch (NotFoundException n){
            log.info("Error getting user detail", n);
            responseData.getMessages().add(n.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
        }catch (Exception e){
            log.error("Error getting user detail", e);
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
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
