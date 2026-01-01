package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.dto.UserDTO;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/get_all_users")
    public ResponseEntity<ResponseData<Page<UserDTO>>> getAllUserData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) throws ExecutionException, InterruptedException {

        ResponseData<Page<UserDTO>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<User> usersPage = userService.getAllUsers(pageable);

        responseData.getMessages().add("No users found");
        responseData.setStatus(false);
        if (!usersPage.isEmpty()) {
            responseData.setStatus(true);
            responseData.getMessages().removeFirst();
            responseData.getMessages().add("Retrieved page " + page + " of users"
            );
        }

        Page<UserDTO> userDTOS = usersPage
                .map(user -> modelMapper.map(user, UserDTO.class));

        responseData.setPayload(userDTOS);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }
}
