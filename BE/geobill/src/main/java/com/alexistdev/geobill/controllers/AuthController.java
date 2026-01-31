package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.AuthDTO;
import com.alexistdev.geobill.dto.MenuDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.request.LoginRequest;
import com.alexistdev.geobill.request.RegisterRequest;
import com.alexistdev.geobill.services.MenuService;
import com.alexistdev.geobill.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200/login")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;
    private final MenuService menuService;

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    public AuthController(UserService userService, ModelMapper modelMapper, MessageSource messageSource, MenuService menuService) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.messageSource = messageSource;
        this.menuService = menuService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseData<User>> register(@Valid @RequestBody RegisterRequest userRequest, Errors errors) {
        ResponseData<User> responseData = new ResponseData<>();
        handleErrors(errors, responseData);

        if(!responseData.isStatus()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        try {
            String msgSuccess = this.messageLocale( "authcontroller.register.success");
            responseData.setPayload(userService.registerUser(userRequest));
            responseData.getMessages().add(msgSuccess);
            responseData.setStatus(true);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
        }catch (Exception e) {
            responseData.setStatus(false);
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseData<AuthDTO>> login(@Valid @RequestBody LoginRequest loginRequest, Errors errors) {
        ResponseData<AuthDTO> responseData = new ResponseData<>();
        handleErrors(errors, responseData);

        User user = userService.authenticate(loginRequest);

        if (user != null) {
            AuthDTO result =  modelMapper.map(user, AuthDTO.class);
            String role = result.getRole();

            String homeUser = "/users/dashboard";
            result.setHomeURL(homeUser);

            if(role.equals(Role.ADMIN.toString())){
                String homeAdmin = "/admin/dashboard";
                result.setHomeURL(homeAdmin);
            }

            if(role.equals(Role.STAFF.toString())){
                String homeStaff = "/staff/dashboard";
                result.setHomeURL(homeStaff);
            }

            List<MenuDTO> menus = menuService.getMenusByRole(user.getRole())
                     .stream()
                     .map(menu-> modelMapper.map(menu, MenuDTO.class))
                     .collect(Collectors.toList());
            String validMsg = String.format(this.messageLocale("authcontroller.login.valid"), result.getEmail());
            logger.info(validMsg);
            result.setMenus(menus);
            responseData.setPayload(result);
            responseData.getMessages().add(this.messageLocale("authcontroller.login.success"));
            responseData.setStatus(true);
            return ResponseEntity.status(HttpStatus.OK).body(responseData);
        }
        String invalidMsg = String.format(this.messageLocale("authcontroller.login.invalid"), loginRequest.getEmail());
        logger.info(invalidMsg);
        responseData.setStatus(false);
        responseData.getMessages().add(invalidMsg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
    }

    private void handleErrors(Errors errors, ResponseData<?> responseData){
        if (errors.hasErrors()) {
            for (ObjectError error : errors.getAllErrors()) {
                logger.info(error.getDefaultMessage());
                responseData.getMessages().add(error.getDefaultMessage());
            }
            responseData.setStatus(false);
        } else {
            responseData.setStatus(true);
        }
    }

    private String messageLocale(String key){
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
