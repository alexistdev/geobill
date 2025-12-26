package com.alexistdev.geobill.service;

import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.UserRepo;
import com.alexistdev.geobill.request.LoginRequest;
import com.alexistdev.geobill.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");
    }

    @Test
    @DisplayName("Test load User by Username and then return UserDetails")
    void loadUserByUsername_UserFound_ReturnsUserDetails() {
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());

        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals(user.getEmail(), userDetails.getUsername());
    }

    @Test
    @DisplayName("Test load User by Username and then throw UsernameNotFoundException")
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        Assertions.assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(user.getEmail()));
    }

    @Test
    @DisplayName("Test Register User")
    void registerUser_NewUser_ReturnRegisteredUser() {
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("hashedPassword");
        when(userRepo.save(any(User.class))).thenReturn(user);

        User registeredUser = userService.registerUser(user);

        Assertions.assertNotNull(registeredUser);
        Assertions.assertEquals(Role.USER, registeredUser.getRole());
        Assertions.assertEquals("hashedPassword", registeredUser.getPassword());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Test Register User with Existing User")
    void registerUser_ExistingUser_ThrowsRuntimeException() {
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Assertions.assertThrows(RuntimeException.class, () -> userService.registerUser(user));
    }

    @Test
    @DisplayName("Test Get All Users and then Return Page of Users")
    void getAllUsers_ReturnsPageOfUsers() {
        List<User> users = new ArrayList<>();
        users.add(user);
        Page<User> pageOfUsers = new PageImpl<>(users);

        when(userRepo.findByRoleNot(Role.ADMIN, Pageable.unpaged())).thenReturn(pageOfUsers);

        Page<User> allUsers = userService.getAllUsers(Pageable.unpaged());

        Assertions.assertNotNull(allUsers);
        Assertions.assertEquals(1, allUsers.getContent().size());
        Assertions.assertEquals(user, allUsers.getContent().getFirst());
    }

    @Test
    @DisplayName("Test Authenticate User when Valid then Return User")
    void authenticate_ValidCredentials_ReturnsUser() {
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);

        User authenticatedUser = userService.authenticate(loginRequest);

        Assertions.assertNotNull(authenticatedUser);
        Assertions.assertEquals(user, authenticatedUser);
    }

    @Test
    @DisplayName("Test Authenticate User when Invalid then Return Null")
    void authenticate_InvalidPassword_ReturnsNull() {
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        User authenticatedUser = userService.authenticate(loginRequest);
        Assertions.assertNull(authenticatedUser);
    }

    @Test
    @DisplayName("Test Authenticate User when User Not Found then Return Null")
    void authenticate_UserNotFound_ReturnsNull() {
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        User authenticatedUser = userService.authenticate(loginRequest);
        Assertions.assertNull(authenticatedUser);
    }
}
