package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.AuthDTO;
import com.alexistdev.geobill.dto.MenuDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.exceptions.SuspendedException;
import com.alexistdev.geobill.models.entity.Menu;
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

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200/login")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;
    private final MenuService menuService;
    private final RateLimiter rateLimiter;

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    public AuthController(UserService userService, ModelMapper modelMapper, MessageSource messageSource, MenuService menuService) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.messageSource = messageSource;
        this.menuService = menuService;

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .limitForPeriod(5)
                .timeoutDuration(Duration.ofSeconds(3))
                .build();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        this.rateLimiter = registry.rateLimiter("rateLimiter");

    }

    @PostMapping("/register")
    public ResponseEntity<ResponseData<User>> register(@Valid @RequestBody RegisterRequest userRequest, Errors errors) {
        ResponseData<User> responseData = new ResponseData<>();
        handleErrors(errors, responseData);

        if (!responseData.isStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        try {
            String msgSuccess = this.messageLocale("authcontroller.register.success");
            responseData.setPayload(userService.registerUser(userRequest));
            responseData.getMessages().add(msgSuccess);
            responseData.setStatus(true);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
        } catch (Exception e) {
            responseData.setStatus(false);
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseData<AuthDTO>> login(@Valid @RequestBody LoginRequest loginRequest, Errors errors) {
        Supplier<ResponseEntity<ResponseData<AuthDTO>>> loginAttempt = () -> {
            ResponseData<AuthDTO> responseData = new ResponseData<>();
            handleErrors(errors, responseData);

            try {
                String msgSuccess = this.messageLocale("authcontroller.login.success");
                responseData.getMessages().add(msgSuccess);
                User user = userService.authenticate(loginRequest);
                AuthDTO result = modelMapper.map(user, AuthDTO.class);
                String role = result.getRole();

                List<MenuDTO> menus = menuService.getMenusByRole(user.getRole())
                        .stream()
                        .map(menu -> modelMapper.map(menu, MenuDTO.class))
                        .collect(Collectors.toList());

                result.setMenus(menus);

                String homeUser = "/users/dashboard";
                result.setHomeURL(homeUser);

                if (role.equals(Role.ADMIN.toString())) {
                    String homeAdmin = "/admin/dashboard";
                    result.setHomeURL(homeAdmin);
                }

                if (role.equals(Role.STAFF.toString())) {
                    String homeStaff = "/staff/dashboard";
                    result.setHomeURL(homeStaff);
                }
                responseData.setPayload(result);
                responseData.setStatus(true);
                return ResponseEntity.status(HttpStatus.OK).body(responseData);
            } catch (SuspendedException s) {
                logger.info(s.getMessage());
                responseData.getMessages().removeFirst();
                String invalidMsg = s.getMessage();
                responseData.setStatus(false);
                responseData.setPayload(null);
                responseData.getMessages().add(invalidMsg);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseData);
            } catch (Exception e) {
                logger.info(e.getMessage());
                responseData.setStatus(false);
                responseData.setPayload(null);
                responseData.getMessages().removeFirst();
                responseData.getMessages().add(e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
            }
        };

        Supplier<ResponseEntity<ResponseData<AuthDTO>>> restrictedLoginAttempt = RateLimiter.decorateSupplier(rateLimiter, loginAttempt);
        try {
            return restrictedLoginAttempt.get();
        } catch (io.github.resilience4j.ratelimiter.RequestNotPermitted e) {
            String msgRatelimit = this.messageLocale("ratelimiter.restricted.message");
            ResponseData<AuthDTO> responseData = new ResponseData<>();
            responseData.setStatus(false);
            responseData.getMessages().add(msgRatelimit);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(responseData);
        }
    }

    private void handleErrors(Errors errors, ResponseData<?> responseData) {
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

    private String messageLocale(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
