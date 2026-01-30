package com.alexistdev.geobill.service;

import com.alexistdev.geobill.models.entity.Customer;
import com.alexistdev.geobill.models.repository.CustomerRepo;
import com.alexistdev.geobill.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class CustomerServiceTest {
    @Mock
    private CustomerRepo customerRepo;

    @InjectMocks
    private CustomerService customerService;

    private UUID uuid;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UUID uuid = UUID.randomUUID();
    }

    @Test
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
}
