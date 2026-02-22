package com.alexistdev.geobill.service;

import com.alexistdev.geobill.dto.UserDTO;
import com.alexistdev.geobill.dto.UserDetailDTO;
import com.alexistdev.geobill.dto.CustomerDTO;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.exceptions.SuspendedException;
import com.alexistdev.geobill.models.entity.Customer;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.CustomerRepo;
import com.alexistdev.geobill.models.repository.UserRepo;
import com.alexistdev.geobill.request.LoginRequest;
import com.alexistdev.geobill.request.RegisterRequest;
import com.alexistdev.geobill.request.UpdateUserRequest;
import com.alexistdev.geobill.services.CustomerService;
import com.alexistdev.geobill.services.UserService;
import com.alexistdev.geobill.utils.MessagesUtils;
import org.junit.jupiter.api.*;
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
import java.util.Date;
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
    private CustomerRepo customerRepo;

    @Mock
    private MessagesUtils messagesUtils;

    private User user;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private Customer customer;
    private UpdateUserRequest updateUserRequest;

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
        user.setFullName(fullName);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setRole(role);
        user.setSuspended(false);
        user.setCreatedDate(new Date());
        user.setModifiedDate(new Date());

        loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setFullName(fullName);
        registerRequest.setPassword(password);

        customer = new Customer();

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFullName("Updated User");
        updateUserRequest.setBusinessName("Updated Business");
        updateUserRequest.setAddress1("Updated Address 1");
        updateUserRequest.setAddress2("Updated Address 2");
        updateUserRequest.setCity("Updated City");
        updateUserRequest.setState("Updated State");
        updateUserRequest.setCountry("Updated Country");
        updateUserRequest.setPostCode("Updated PostCode");
        updateUserRequest.setPhoneNumber("Updated Phone Number");
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

        UserDTO registeredUser = userService.registerUser(registerRequest);

        Assertions.assertNotNull(registeredUser);
        Assertions.assertEquals(Role.USER.toString(), registeredUser.getRole());
        verify(userRepo, times(1)).save(any(User.class));
        verify(customerService, times(1)).addCustomer(any(Customer.class));
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Register User with Existing User")
    void registerUser_ExistingUser_ThrowsRuntimeException() {
        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));
        when(messagesUtils.getMessage(eq("userservice.user.exist"),  any())).thenReturn("User test@example.com already exist");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(registerRequest));
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
        when(messagesUtils.getMessage(eq("userservice.user.authfailed"), eq(loginRequest.getEmail()))).thenReturn("Authentication failed, password is invalid");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.authenticate(loginRequest));
        Assertions.assertEquals("Authentication failed, password is invalid", exception.getMessage());
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Authenticate User when User Not Found then Throw RuntimeException")
    void authenticate_UserNotFound_ThrowsRuntimeException() {
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());
        when(messagesUtils.getMessage(eq("userservice.user.authfailed"), eq(loginRequest.getEmail()))).thenReturn("Authentication failed, user not found");

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
        when(messagesUtils.getMessage(eq("userservice.user.suspended"))).thenReturn("User is suspended");

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
        when(messagesUtils.getMessage(eq("userservice.user.notfound"), anyString())).thenReturn("User not found");
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        User foundUser = userService.findUserByUUID(userId);

        Assertions.assertNotNull(foundUser);
        Assertions.assertEquals(user, foundUser);
    }

    @Test
    @Order(12)
    @DisplayName("12. Test Find User By UUID when User Not Found then Throw NotFoundException")
    void findUserByUUID_UserNotFound_ThrowsNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(messagesUtils.getMessage(eq("userservice.user.notfound"), anyString())).thenReturn("User not found");
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,
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
        customer.setBusinessName("Test Business");
        customer.setAddress1("123 Main St");
        customer.setAddress2("Suite 100");
        customer.setCity("Test City");
        customer.setState("Test State");
        customer.setCountry("Test Country");
        customer.setPostCode("12345");
        customer.setCustomerNumber(1L);
        customer.setPhone("123-456-7890");
        when(messagesUtils.getMessage(eq("userservice.user.notfound"), anyString())).thenReturn("User not found");
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(customerService.findCustomerByUserId(user)).thenReturn(customer);

        UserDetailDTO userDetailDTO = userService.getUserDetail(userId);

        Assertions.assertNotNull(userDetailDTO);
        Assertions.assertEquals(userId.toString(), userDetailDTO.getId());
        Assertions.assertEquals("Test User", userDetailDTO.getFullName());
        Assertions.assertEquals(user.getEmail(), userDetailDTO.getEmail());
        Assertions.assertEquals(user.getRole().toString(), userDetailDTO.getRole());
        Assertions.assertEquals(user.getCreatedDate(), userDetailDTO.getCreatedDate());
        Assertions.assertEquals(user.getModifiedDate(), userDetailDTO.getModifiedDate());

        Assertions.assertNotNull(userDetailDTO.getCustomer());
        CustomerDTO customerDTO = userDetailDTO.getCustomer();
        Assertions.assertEquals(customer.getId().toString(), customerDTO.getId());
        Assertions.assertEquals(customer.getBusinessName(), customerDTO.getBusinessName());
        Assertions.assertEquals(customer.getAddress1(), customerDTO.getAddress1());
        Assertions.assertEquals(customer.getAddress2(), customerDTO.getAddress2());
        Assertions.assertEquals(customer.getCity(), customerDTO.getCity());
        Assertions.assertEquals(customer.getState(), customerDTO.getState());
        Assertions.assertEquals(customer.getCountry(), customerDTO.getCountry());
        Assertions.assertEquals(customer.getPostCode(), customerDTO.getPostCode());
        Assertions.assertEquals(customer.getPhone(), customerDTO.getPhone());

        verify(userRepo, times(1)).findById(userId);
        verify(customerService, times(1)).findCustomerByUserId(user);
    }

    @Test
    @Order(14)
    @DisplayName("14. Test Update User when User Exists and is not Admin or Deleted")
    void updateUser_UserExistsNotAdminNotDeleted_ReturnsUpdatedUser() {
        UUID userId = user.getId();

        when(messagesUtils.getMessage(eq("userservice.user.notfound"), anyString())).thenReturn("User not found");
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(userRepo.save(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0));
        when(customerService.findCustomerByUserId(user)).thenReturn(customer);
        when(customerRepo.save(any(Customer.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        UserDetailDTO updatedUser = userService.updateUser(userId, updateUserRequest);

        Assertions.assertNotNull(updatedUser);
        Assertions.assertEquals(updateUserRequest.getFullName(), updatedUser.getFullName());

        Assertions.assertNotNull(updatedUser.getCustomer());
        CustomerDTO updatedCustomerDTO = updatedUser.getCustomer();

        Assertions.assertEquals(updateUserRequest.getBusinessName(), updatedCustomerDTO.getBusinessName());
        Assertions.assertEquals(updateUserRequest.getAddress1(), updatedCustomerDTO.getAddress1());
        Assertions.assertEquals(updateUserRequest.getAddress2(), updatedCustomerDTO.getAddress2());
        Assertions.assertEquals(updateUserRequest.getCity(), updatedCustomerDTO.getCity());
        Assertions.assertEquals(updateUserRequest.getState(), updatedCustomerDTO.getState());
        Assertions.assertEquals(updateUserRequest.getCountry(), updatedCustomerDTO.getCountry());
        Assertions.assertEquals(updateUserRequest.getPostCode(), updatedCustomerDTO.getPostCode());
        Assertions.assertEquals(updateUserRequest.getPhoneNumber(), updatedCustomerDTO.getPhone());

        verify(userRepo, times(1)).findById(userId);
        verify(userRepo, times(1)).save(any(User.class));
        verify(customerService, times(1)).findCustomerByUserId(user);
        verify(customerRepo, times(1)).save(any(Customer.class));
    }

    @Test
    @Order(15)
    @DisplayName("15. Test Update User when User Not Found then Throw RuntimeException")
    void updateUser_UserNotFound_ThrowsRuntimeException() {
        UUID userId = UUID.randomUUID();
        when(userRepo.findById(userId)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage(eq("userservice.user.notfound"), eq(String.valueOf(userId))))
                .thenReturn("User not found");

        Assertions.assertThrows(RuntimeException.class, ()
                -> userService.updateUser(userId, updateUserRequest));

        verify(userRepo, times(1)).findById(userId);
        verify(userRepo, never()).save(any(User.class));
        verify(customerService, never()).findCustomerByUserId(user);
        verify(customerRepo, never()).save(any(Customer.class));
    }

    @Test
    @Order(16)
    @DisplayName("16. Test Update User when User is Admin then Throw RuntimeException")
    void updateUser_UserIsAdmin_ThrowsRuntimeException() {
        user.setRole(Role.ADMIN);
        UUID userId = user.getId();

        when(messagesUtils.getMessage(eq("userservice.user.notfound"), anyString())).thenReturn("User not found");
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(messagesUtils.getMessage(eq("userservice.user.admin_update_not_allowed")))
                .thenReturn("Admin User update not allowed");

        Assertions.assertThrows(RuntimeException.class, ()
                -> userService.updateUser(userId, updateUserRequest));

        verify(userRepo, times(1)).findById(userId);
        verify(userRepo, never()).save(any(User.class));
        verify(customerService, never()).findCustomerByUserId(user);
        verify(customerRepo, never()).save(any(Customer.class));
    }

    @Test
    @Order(17)
    @DisplayName("17. Test Update User when User is Deleted then Throw RuntimeException")
    void updateUser_UserIsDeleted_ThrowsRuntimeException() {
        user.setIsDeleted(true);
        UUID userId = user.getId();

        when(messagesUtils.getMessage(eq("userservice.user.notfound"), anyString())).thenReturn("User not found");
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(messagesUtils.getMessage(eq("userservice.user.deleted_user_update_failed")))
                .thenReturn("Deleted user update not allowed");

        Assertions.assertThrows(RuntimeException.class, ()
                -> userService.updateUser(userId, updateUserRequest));

        verify(userRepo, times(1)).findById(userId);
        verify(userRepo, never()).save(any(User.class));
        verify(customerService, never()).findCustomerByUserId(user);
        verify(customerRepo, never()).save(any(Customer.class));
    }

    @Test
    @Order(18)
    @DisplayName("18. Test Update Customer")
    void updateCustomer_UpdatesCustomerSuccessfully() {
        UpdateUserRequest request = new UpdateUserRequest();
        String newBusinessName = "New Business Name";
        String newAddress1 = "New Address 1";
        String newAddress2 = "New Address 2";
        String newCity = "New City";
        String newState = "New State";
        String newCountry = "New Country";
        String newPostCode = "New PostCode";
        String newPhone = "New Phone";

        request.setBusinessName(newBusinessName);
        request.setAddress1(newAddress1);
        request.setAddress2(newAddress2);
        request.setCity(newCity);
        request.setState(newState);
        request.setCountry(newCountry);
        request.setPostCode(newPostCode);
        request.setPhoneNumber(newPhone);

        Customer customer = new Customer();
        customer.setCustomerNumber(1L);

        when(customerService.findCustomerByUserId(user)).thenReturn(customer);
        when(customerRepo.save(any(Customer.class))).thenReturn(customer);

        userService.updateCustomer(user, request);

        Assertions.assertEquals(newBusinessName, customer.getBusinessName());
        Assertions.assertEquals(newAddress1, customer.getAddress1());
        Assertions.assertEquals(newAddress2, customer.getAddress2());
        Assertions.assertEquals(newCity, customer.getCity());
        Assertions.assertEquals(newState, customer.getState());
        Assertions.assertEquals(newCountry, customer.getCountry());
        Assertions.assertEquals(newPostCode, customer.getPostCode());
        Assertions.assertEquals(newPhone, customer.getPhone());

        verify(customerService, times(1)).findCustomerByUserId(user);
        verify(customerRepo, times(1)).save(any(Customer.class));
    }


}

