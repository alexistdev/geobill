package com.alexistdev.geobill.services;

import com.alexistdev.geobill.models.entity.Customer;
import com.alexistdev.geobill.models.repository.CustomerRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerService {

    private final CustomerRepo customerRepo;


    public CustomerService(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    public Customer addCustomer(Customer customer) {
        return customerRepo.save(customer);
    }
}
