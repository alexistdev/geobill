package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Customer;
import com.alexistdev.geobill.models.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CustomerRepo extends JpaRepository<Customer, UUID> {

//    @Query("SELECT p FROM Customer p LEFT JOIN FETCH User pt WHERE  pt.isDeleted = false")
//    Page<Product> findAllCustomers(Pageable pageable);

    Customer findByUser_Id(UUID userId);
}
