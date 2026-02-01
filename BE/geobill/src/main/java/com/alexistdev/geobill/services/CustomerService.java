package com.alexistdev.geobill.services;

import com.alexistdev.geobill.models.entity.Customer;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.CustomerRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerService {

    private final CustomerRepo customerRepo;
    private final MessageSource messageSource;


    public CustomerService(CustomerRepo customerRepo, MessageSource messageSource) {
        this.customerRepo = customerRepo;
        this.messageSource = messageSource;
    }

    public Customer addCustomer(Customer customer) {
        return customerRepo.save(customer);
    }

    public Customer findCustomerByUserId(User user){
       Customer foundCustomer = customerRepo.findByUser_Id(user.getId());
       if(foundCustomer == null){
           String notFoundMessage = String.format(
                   messageSource.getMessage("customer.userId.notfound", null,
                           LocaleContextHolder.getLocale()), user.getId());
           throw new RuntimeException(notFoundMessage);
       }
       return foundCustomer;
    }
}
