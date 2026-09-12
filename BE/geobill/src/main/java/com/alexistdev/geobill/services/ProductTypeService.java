package com.alexistdev.geobill.services;

import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.utils.MessagesUtils;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.repository.ProductTypeRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ProductTypeService {

    private final ProductTypeRepo productTypeRepo;
    private final MessagesUtils messagesUtils;

    public ProductTypeService(ProductTypeRepo productTypeRepo, MessagesUtils messagesUtils) {
        this.productTypeRepo = productTypeRepo;
        this.messagesUtils = messagesUtils;
    }

    public Page<ProductType> getAllProductTypes(Pageable pageable){
        return productTypeRepo.findByIsDeletedFalse(pageable);
    }

    public Page<ProductType> getAllProductTypesByFilter(Pageable pageable, String keyword) {
        return productTypeRepo.findByFilter(keyword.toLowerCase(), pageable);
    }

    public ProductType save(ProductType productType) {
        Optional<ProductType> foundProductType = productTypeRepo.findByNameIncludingDeleted(productType.getName());
        if(foundProductType.isPresent()){
            ProductType existing = foundProductType.get();

            if(!existing.getDeleted()){
                log.info("ProductType with name '" + productType.getName() + "' already exists");
               throw new DuplicateException(messagesUtils.getMessage("producttypeservice.name_exist", productType.getName()));
            }

            existing.setDeleted(false);
            productType = existing;
        }

        return productTypeRepo.save(productType);
    }

    public ProductType update(UUID id, ProductType productType) {
        ProductType existingProductType = productTypeRepo.findById(id)
                .orElseThrow(()-> new IllegalArgumentException(messagesUtils.getMessage("producttypeservice.not_found", id.toString())));

        if(existingProductType.getDeleted()){
            existingProductType.setName(productType.getName());
            existingProductType.setDeleted(false);
            productType = existingProductType;
        }

        return productTypeRepo.save(productType);
    }

    public void delete(UUID id) {
        ProductType productType = productTypeRepo.findById(id)
                .orElseThrow(()-> new IllegalArgumentException(messagesUtils.getMessage("producttypeservice.not_found", id.toString())));
        productType.setDeleted(true);
        productTypeRepo.save(productType);
    }

    public ProductType findByUUID(UUID uuid){
        return productTypeRepo.findById(uuid).orElse(null);
    }

}
