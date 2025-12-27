package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.dto.UserDTO;
import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.request.LoginRequest;
import com.alexistdev.geobill.request.RegisterRequest;
import com.alexistdev.geobill.services.MenuService;
import com.alexistdev.geobill.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200/login")
@RestController
@RequestMapping("/v1/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MenuService menuService;

    @PostMapping("/register")
    public ResponseEntity<ResponseData<User>> register(@Valid @RequestBody RegisterRequest userRequest, Errors errors) {
        ResponseData<User> responseData = new ResponseData<>();
        handleErrors(errors, responseData);

        if(!responseData.isStatus()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        try {
            User user = modelMapper.map(userRequest, User.class);
            responseData.setPayload(userService.registerUser(user));
            responseData.setStatus(true);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
        }catch (Exception e) {
            responseData.setStatus(false);
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseData<UserDTO>> login(@Valid @RequestBody LoginRequest loginRequest, Errors errors) {
        ResponseData<UserDTO> responseData = new ResponseData<>();
        handleErrors(errors, responseData);

        User user = userService.authenticate(loginRequest);

        if (user != null) {
            UserDTO result =  modelMapper.map(user, UserDTO.class);

            List<Menu> menus = menuService.getMenusByRole(user.getRole());

            result.setMenus(menus);
            responseData.setPayload(result);
            responseData.getMessages().add("User is valid");
            responseData.setStatus(true);
            return ResponseEntity.status(HttpStatus.OK).body(responseData);
        }
        responseData.setStatus(false);
        responseData.getMessages().add("Invalid username or password");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
    }

    private void handleErrors(Errors errors, ResponseData<?> responseData){
        if (errors.hasErrors()) {
            for (ObjectError error : errors.getAllErrors()) {
                responseData.getMessages().add(error.getDefaultMessage());
            }
            responseData.setStatus(false);
        } else {
            responseData.setStatus(true);
        }
    }
}
