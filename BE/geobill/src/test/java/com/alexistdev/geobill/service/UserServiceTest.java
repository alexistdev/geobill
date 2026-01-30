package com.alexistdev.geobill.service;

import com.alexistdev.geobill.models.entity.Customer;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.UserRepo;
import com.alexistdev.geobill.request.LoginRequest;
import com.alexistdev.geobill.request.RegisterRequest;
import com.alexistdev.geobill.services.CustomerService;
import com.alexistdev.geobill.services.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    @Mock
    private CustomerService customerService;

    @Mock
    private MessageSource messageSource;

    private User user;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private Customer customer;


    @BeforeEach
    void setUp() {
        String email = "test@example.com";
        String password = "password";
        String fullName = "Test User";
        Role role = Role.USER;

        user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setFullName(fullName);
        registerRequest.setPassword(password);

        customer = new Customer();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test load User by Username and then return UserDetails")
    void loadUserByUsername_UserFound_ReturnsUserDetails() {
        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(registerRequest.getEmail());

        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals(registerRequest.getEmail(), userDetails.getUsername());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test load User by Username and then throw UsernameNotFoundException")
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        Assertions.assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(registerRequest.getEmail()));
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Register User")
    void registerUser_NewUser_ReturnRegisteredUser() {
        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(customerService.addCustomer(any(Customer.class))).thenReturn(customer);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = userService.registerUser(registerRequest);

        Assertions.assertNotNull(registeredUser);
        Assertions.assertEquals(Role.USER, registeredUser.getRole());
        Assertions.assertEquals("hashedPassword", registeredUser.getPassword());
        verify(userRepo, times(1)).save(any(User.class));
        verify(customerService, times(1)).addCustomer(any(Customer.class));
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Register User with Existing User")
    void registerUser_ExistingUser_ThrowsRuntimeException() {
        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));
        when(messageSource.getMessage(eq("userservice.user.exist"), any(), any())).thenReturn("User %s already exist");
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> userService.registerUser(registerRequest));
        System.out.println("[DEBUG_LOG] Exception message: " + exception.getMessage());
        Assertions.assertEquals("User " + registerRequest.getEmail() + " already exist", exception.getMessage());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Get All Users and then Return Page of Users")
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
    @Order(6)
    @DisplayName("6. Test Authenticate User when Valid then Return User")
    void authenticate_ValidCredentials_ReturnsUser() {
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);

        User authenticatedUser = userService.authenticate(loginRequest);

        Assertions.assertNotNull(authenticatedUser);
        Assertions.assertEquals(user, authenticatedUser);
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Authenticate User when Invalid then Return Null")
    void authenticate_InvalidPassword_ReturnsNull() {
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);
        when(messageSource.getMessage(eq("userservice.user.authfailed"), any(), any())).thenReturn("Authentication failed, password is invalid");

        User authenticatedUser = userService.authenticate(loginRequest);
        Assertions.assertNull(authenticatedUser);
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Authenticate User when User Not Found then Return Null")
    void authenticate_UserNotFound_ReturnsNull() {
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        User authenticatedUser = userService.authenticate(loginRequest);
        Assertions.assertNull(authenticatedUser);
    }
}
