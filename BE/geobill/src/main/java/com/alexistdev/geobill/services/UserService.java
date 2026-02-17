package com.alexistdev.geobill.services;

import com.alexistdev.geobill.dto.CustomerDTO;
import com.alexistdev.geobill.dto.UserDetailDTO;
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final CustomerService customerService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MessageSource messageSource;
    private final CustomerRepo customerRepo;

    public UserService(UserRepo userRepo, CustomerService customerService,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            MessageSource messageSource, CustomerRepo customerRepo) {
        this.userRepo = userRepo;
        this.customerService = customerService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.messageSource = messageSource;
        this.customerRepo = customerRepo;
    }

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(String
                .format("User %s not found", email)));
    }

    @Transactional
    public User updateUser(UUID id, UpdateUserRequest request) {
        User user = this.findUserByUUID(id);

        if (user == null) {
            String userNotFoundMessage = String.format(
                    messageSource.getMessage("customer.userId.notfound", null,
                            LocaleContextHolder.getLocale()),
                    id);
            logger.warning(userNotFoundMessage);
            throw new RuntimeException(userNotFoundMessage);
        }

        if (user.getRole().equals(Role.ADMIN)) {
            String userAdminNotAllowed = messageSource.getMessage("userservice.user.admin_update_not_allowed",
                    null, LocaleContextHolder.getLocale());
            logger.warning(userAdminNotAllowed);
            throw new RuntimeException(userAdminNotAllowed);
        }

        if (user.getIsDeleted()) {
            String userDeletedNotAllowedMessage = messageSource.getMessage(
                    "userservice.user.deleted_user_update_failed",
                    null, LocaleContextHolder.getLocale());
            logger.warning(userDeletedNotAllowedMessage);
            throw new RuntimeException(userDeletedNotAllowedMessage);
        }

        user.setFullName(request.getFullName());
        User updatedUser = userRepo.save(user);

        try {
            this.updateCustomer(user, request);
        } catch (Exception e) {
            String failedToUpdateCustomer = messageSource.getMessage("userservice.user.customer_update_failed",
                    null, LocaleContextHolder.getLocale());
            logger.warning(failedToUpdateCustomer + e.getMessage());
            throw new RuntimeException(failedToUpdateCustomer, e);
        }

        return updatedUser;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void updateCustomer(User user, UpdateUserRequest request) {
        Customer customer = customerService.findCustomerByUserId(user);
        customer.setBusinessName(request.getBusinessName());
        customer.setAddress1(request.getAddress1());
        customer.setAddress2(request.getAddress2());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setPostCode(request.getPostCode());
        customer.setPhone(request.getPhoneNumber());
        customerRepo.save(customer);
    }

    public User registerUser(RegisterRequest request) {
        boolean userExist = userRepo.findByEmail(request.getEmail()).isPresent();
        if (userExist) {
            String message = String.format(messageSource.getMessage("userservice.user.exist",
                    null, LocaleContextHolder.getLocale()), request.getEmail());
            logger.info(message);
            throw new RuntimeException(message);
        }
        Date now = new Date();
        User userSaved = new User();
        userSaved.setFullName(request.getFullName());
        userSaved.setEmail(request.getEmail());
        userSaved.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        userSaved.setRole(Role.USER);
        userSaved.setCreatedBy("System");
        userSaved.setModifiedBy("System");
        userSaved.setCreatedDate(now);
        userSaved.setModifiedDate(now);

        User userResult = userRepo.save(userSaved);

        Customer customerSaved = new Customer();
        customerSaved.setUser(userResult);
        customerService.addCustomer(customerSaved);

        return userResult;
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepo.findByRoleNot(Role.ADMIN, pageable);
    }

    public Page<User> getAllUsersByFilter(Pageable pageable, String keyword) {
        return userRepo.findByFilter(keyword.toLowerCase(), pageable);
    }

    public User authenticate(LoginRequest loginRequest) {
        Optional<User> userExist = userRepo.findByEmail(loginRequest.getEmail());

        if (userExist.isPresent()) {
            boolean authCheck = bCryptPasswordEncoder.matches(loginRequest.getPassword(),
                    userExist.get().getPassword());

            if (!authCheck) {
                String authFailedMessage = String.format(messageSource.getMessage("userservice.user.authfailed",
                        null, LocaleContextHolder.getLocale()), loginRequest.getEmail());
                logger.info(authFailedMessage);
                throw new RuntimeException(authFailedMessage);
            }

            if (userExist.get().isSuspended()) {
                String userSuspendedMessage = messageSource.getMessage("userservice.user.suspended",
                        null, LocaleContextHolder.getLocale());
                logger.info(userSuspendedMessage);
                throw new SuspendedException(userSuspendedMessage);
            }

            return userExist.get();
        }
        String authFailedMessage = String.format(messageSource.getMessage("userservice.user.authfailed",
                null, LocaleContextHolder.getLocale()), loginRequest.getEmail());
        throw new RuntimeException(authFailedMessage);
    }

    public User findUserByUUID(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("userservice.user.notfound", null, LocaleContextHolder.getLocale())
                                + " " + id));
    }

    public UserDetailDTO getUserDetail(UUID userId) {
        User userResult = this.findUserByUUID(userId);
        Customer customerResult = customerService.findCustomerByUserId(userResult);
        UserDetailDTO userDetailDTO = new UserDetailDTO();

        userDetailDTO.setId(userResult.getId().toString());
        userDetailDTO.setFullName(userResult.getFullName());
        userDetailDTO.setEmail(userResult.getEmail());
        userDetailDTO.setRole(userResult.getRole().toString());
        userDetailDTO.setCreatedDate(userResult.getCreatedDate());
        userDetailDTO.setModifiedDate(userResult.getModifiedDate());
        userDetailDTO.setCustomer(this.converToCustomerDTO(customerResult));

        return userDetailDTO;
    }

    private CustomerDTO converToCustomerDTO(Customer customer) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customer.getId().toString());
        customerDTO.setBusinessName(customer.getBusinessName());
        customerDTO.setAddress1(customer.getAddress1());
        customerDTO.setAddress2(customer.getAddress2());
        customerDTO.setCity(customer.getCity());
        customerDTO.setState(customer.getState());
        customerDTO.setCountry(customer.getCountry());
        customerDTO.setPostCode(customer.getPostCode());
        customerDTO.setPhone(customer.getPhone());
        return customerDTO;
    }
}
