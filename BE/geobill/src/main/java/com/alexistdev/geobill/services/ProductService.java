package com.alexistdev.geobill.services;

import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.repository.ProductRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    public Page<Product> getAllProducts(Pageable pageable){
        return productRepo.findByIsDeletedFalse(pageable);
    }

    public Page<Product> getAllProductsByFilter(Pageable pageable, String keyword) {
        return productRepo.findByFilter(keyword.toLowerCase(), pageable);
    }

    public Product save(Product product) {
        Optional<Product> foundProduct = productRepo.findByNameIncludingDeleted(product.getName());
        if(foundProduct.isPresent()){
            Product existing = foundProduct.get();

            if(!existing.getIsDeleted()){
                log.info("Product with name '" + product.getName() + "' already exists");
                throw new DuplicateException("Product with name '" + product.getName() + "' already exists");
            }

            existing.setIsDeleted(false);
            product = existing;
        }
        return productRepo.save(product);
    }

    public Product update(UUID id, Product product) {
        Product existingProduct = productRepo.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Product not found with ID:" + id));

        if(existingProduct.getIsDeleted()){
            existingProduct.setName(product.getName());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setCycle(product.getCycle());
            existingProduct.setCapacity(product.getCapacity());
            existingProduct.setBandwith(product.getBandwith());
            existingProduct.setAddon_domain(product.getAddon_domain());
            existingProduct.setDatabase_account(product.getDatabase_account());
            existingProduct.setFtp_account(product.getFtp_account());
            existingProduct.setInfo1(product.getInfo1());
            existingProduct.setInfo2(product.getInfo2());
            existingProduct.setInfo3(product.getInfo3());
            existingProduct.setInfo4(product.getInfo4());
            existingProduct.setInfo5(product.getInfo5());
            existingProduct.setDeleted(false);
            product = existingProduct;
        }
        return productRepo.save(product);
    }

    public void delete(UUID id) {
        Product product = productRepo.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Product not found with ID:" + id));
        product.setIsDeleted(true);
        productRepo.save(product);
    }

}
