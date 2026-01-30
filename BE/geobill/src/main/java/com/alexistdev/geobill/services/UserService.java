package com.alexistdev.geobill.services;

import com.alexistdev.geobill.models.entity.Customer;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.UserRepo;
import com.alexistdev.geobill.request.LoginRequest;
import com.alexistdev.geobill.request.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", email)));
    }

    public User registerUser(RegisterRequest request) {
        boolean userExist = userRepo.findByEmail(request.getEmail()).isPresent();
        if (userExist) {
            logger.info(String.format("{userservice.user.exist}", request.getEmail()));
            throw new RuntimeException(
                    String.format("{userservice.user.exist}", request.getEmail())
            );
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

    public User authenticate(LoginRequest loginRequest) {
        Optional<User> userExist = userRepo.findByEmail(loginRequest.getEmail());
        if (userExist.isPresent()) {
            boolean authCheck = bCryptPasswordEncoder.matches(loginRequest.getPassword(), userExist.get().getPassword());
            if (!authCheck) {
                logger.info("{userservice.user.authfailed}");
                return null;
            }
            return userExist.get();
        }
        return null;
    }

    public User findUserByUUID(UUID id){
        return userRepo.findById(id).orElseThrow(()-> new IllegalArgumentException("{userservice.user.notfound}" + id));
    }
}
