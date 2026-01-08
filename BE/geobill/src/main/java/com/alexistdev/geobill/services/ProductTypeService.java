package com.alexistdev.geobill.services;

import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.repository.ProductTypeRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ProductTypeService {

    @Autowired
    private ProductTypeRepo productTypeRepo;

    public Page<ProductType> getAllProductTypes(Pageable pageable){
        return productTypeRepo.findByIsDeletedFalse(pageable);
    }

    public ProductType save(ProductType productType) {
        Optional<ProductType> foundProductType = productTypeRepo.findByNameIncludingDeleted(productType.getName());
        if(foundProductType.isPresent()){
            ProductType existing = foundProductType.get();

            if(!existing.getDeleted()){
                log.info("ProductType already exist");
               throw new IllegalArgumentException("ProductType already exist");
            }

            existing.setDeleted(false);
            productType = existing;
        }

        return productTypeRepo.save(productType);
    }

    public ProductType update(UUID id, ProductType productType) {
        ProductType existingProductType = productTypeRepo.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("ProductType not found with ID: " + id));

        if(existingProductType.getDeleted()){
            existingProductType.setName(productType.getName());
            existingProductType.setDeleted(false);
            productType = existingProductType;
        }

        return productTypeRepo.save(productType);
    }

    public void delete(UUID id) {
        ProductType productType = productTypeRepo.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("ProductType not found with ID: " + id));
        productType.setDeleted(true);
        productTypeRepo.save(productType);
    }

}
