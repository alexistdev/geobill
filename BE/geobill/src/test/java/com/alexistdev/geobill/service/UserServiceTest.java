package com.alexistdev.geobill.service;

import com.alexistdev.geobill.dto.UserDetailDTO;
import com.alexistdev.geobill.exceptions.SuspendedException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

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
        UUID id = UUID.randomUUID();

        user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setSuspended(false);

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
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(registerRequest.getEmail()));
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
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(registerRequest));
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
    @DisplayName("7. Test Authenticate User when Invalid then Throw RuntimeException")
    void authenticate_InvalidPassword_ThrowsRuntimeException() {
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);
        when(messageSource.getMessage(eq("userservice.user.authfailed"), any(), any())).thenReturn("Authentication failed, password is invalid");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.authenticate(loginRequest));
        Assertions.assertEquals("Authentication failed, password is invalid", exception.getMessage());
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Authenticate User when User Not Found then Throw RuntimeException")
    void authenticate_UserNotFound_ThrowsRuntimeException() {
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("userservice.user.authfailed"), any(), any())).thenReturn("Authentication failed, user not found");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.authenticate(loginRequest));

        Assertions.assertEquals("Authentication failed, user not found", exception.getMessage());
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Authenticate User when User Suspended then Throw SuspendedException")
    void authenticate_UserSuspended_ThrowsSuspendedException() {
        user.setSuspended(true);
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(messageSource.getMessage(eq("userservice.user.suspended"), any(), any())).thenReturn("User is suspended");

        Assertions.assertThrows(SuspendedException.class,
                () -> userService.authenticate(loginRequest));
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Get All Users By Filter and then Return Page of Users")
    void getAllUsersByFilter_ReturnsPageOfUsers() {
        List<User> users = new ArrayList<>();
        users.add(user);
        Page<User> pageOfUsers = new PageImpl<>(users);
        String keyword = "test";

        when(userRepo.findByFilter(keyword.toLowerCase(), Pageable.unpaged())).thenReturn(pageOfUsers);

        Page<User> allUsers = userService.getAllUsersByFilter(Pageable.unpaged(), keyword);

        Assertions.assertNotNull(allUsers);
        Assertions.assertEquals(1, allUsers.getContent().size());
        Assertions.assertEquals(user, allUsers.getContent().getFirst());
    }

    @Test
    @Order(11)
    @DisplayName("11. Test Find User By UUID when User Exists then Return User")
    void findUserByUUID_UserExists_ReturnsUser() {
        UUID userId = user.getId();
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        User foundUser = userService.findUserByUUID(userId);

        Assertions.assertNotNull(foundUser);
        Assertions.assertEquals(user, foundUser);
    }

    @Test
    @Order(12)
    @DisplayName("12. Test Find User By UUID when User Not Found then Throw IllegalArgumentException")
    void findUserByUUID_UserNotFound_ThrowsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        when(userRepo.findById(userId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("userservice.user.notfound"),
                any(), any())).thenReturn("User not found");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.findUserByUUID(userId));
    }

    @Test
    @Order(13)
    @DisplayName("13. Test Get User Detail and then Return UserDetailDTO")
    void getUserDetail_shouldReturnUserDetailDTO() {
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());

        when(userRepo.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(customerService.findCustomerByUserId(user)).thenReturn(customer);

        UserDetailDTO userDetailDTO = userService.getUserDetail(userId);

        Assertions.assertNotNull(userDetailDTO);
        Assertions.assertEquals(userId.toString(), userDetailDTO.getId());
        Assertions.assertEquals(user.getFullName(), userDetailDTO.getFullName());
        Assertions.assertEquals(user.getEmail(), userDetailDTO.getEmail());
        Assertions.assertEquals(user.getRole().toString(), userDetailDTO.getRole());
        Assertions.assertEquals(user.getCreatedDate(), userDetailDTO.getCreatedDate());
        Assertions.assertEquals(user.getModifiedDate(), userDetailDTO.getModifiedDate());
        Assertions.assertEquals(customer, userDetailDTO.getCustomer());
    }
}
