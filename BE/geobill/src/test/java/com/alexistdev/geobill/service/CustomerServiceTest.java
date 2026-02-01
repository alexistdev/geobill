package com.alexistdev.geobill.service;

import com.alexistdev.geobill.models.entity.Customer;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.CustomerRepo;
import com.alexistdev.geobill.services.CustomerService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerServiceTest {
    @Mock
    private CustomerRepo customerRepo;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private CustomerService customerService;

    private UUID uuid;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        uuid = UUID.randomUUID();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Add Customer")
    void addCustomer() {

        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");

        Customer savedCustomer = new Customer();
        savedCustomer.setId(uuid);
        savedCustomer.setFirstName("John");
        savedCustomer.setLastName("Doe");

        when(customerRepo.save(customer)).thenReturn(savedCustomer);

        Customer newCustomer = customerService.addCustomer(customer);

        assertEquals(savedCustomer.getId(), newCustomer.getId());
        assertEquals(savedCustomer.getFirstName(), newCustomer.getFirstName());
        assertEquals(savedCustomer.getLastName(), newCustomer.getLastName());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Find Customer By User Id")
    void findCustomerByUserId_ExistingUser() {
        User user = new User();
        user.setId(uuid);

        Customer customer = new Customer();
        customer.setId(uuid);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setUser(user);

        when(customerRepo.findByUser_Id(user.getId())).thenReturn(customer);

        Customer foundCustomer = customerService.findCustomerByUserId(user);

        assertEquals(customer.getId(), foundCustomer.getId());
        assertEquals(customer.getFirstName(), foundCustomer.getFirstName());
        assertEquals(customer.getLastName(), foundCustomer.getLastName());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Find Customer By User Non Existing ID")
    void findCustomerByUserId_NonExistingUser() {
        User user = new User();
        user.setId(uuid);

        when(customerRepo.findByUser_Id(user.getId())).thenReturn(null);
        when(messageSource.getMessage("customer.userId.notfound", null,
                LocaleContextHolder.getLocale())).thenReturn("User ID tidak ditemukan");

       RuntimeException exception= Assertions.assertThrows(RuntimeException.class,
                () -> customerService.findCustomerByUserId(user));

        Assertions.assertEquals("User ID tidak ditemukan", exception.getMessage());
    }
}
