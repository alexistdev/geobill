package com.alexistdev.geobill.services;

import com.alexistdev.geobill.dto.ProductDTO;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.repository.ProductRepo;
import com.alexistdev.geobill.request.ProductRequest;
import com.alexistdev.geobill.utils.MessagesUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ProductService {

    private final ProductRepo productRepo;
    private final ProductTypeService productTypeService;
    private final ModelMapper modelMapper;
    private final MessagesUtils messagesUtils;

    public ProductService(ProductRepo productRepo, ProductTypeService productTypeService, ModelMapper modelMapper, MessagesUtils messagesUtils) {
        this.productRepo = productRepo;
        this.productTypeService = productTypeService;
        this.modelMapper = modelMapper;
        this.messagesUtils = messagesUtils;
    }

    public Page<Product> getAllProducts(Pageable pageable){
        return productRepo.findByIsDeletedFalse(pageable);
    }

    public Page<Product> getAllProductsByFilter(Pageable pageable, String keyword) {
        return productRepo.findByFilter(keyword.toLowerCase(), pageable);
    }

    public ProductDTO findById(UUID id) {
        Optional<Product> foundProduct = productRepo.findById(id);
        String message = messagesUtils.getMessage("productservice.product.not_found",
                String.valueOf(id));
        if(foundProduct.isPresent()){
            Product product = foundProduct.get();
            if(product.getIsDeleted() || product.getProductType().getIsDeleted()){
                throw new NotFoundException(message);
            }
            return convertToProductDTO(product);
        }
        throw new NotFoundException(message);
    }

    public Page<Product> getAllProductsByProductTypeId(Pageable pageable, UUID productTypeId) {
        return productRepo.findByProductTypeId(productTypeId, pageable);
    }

    @Transactional
    public Product save(ProductRequest request) {
        Product saveProduct = new Product();

        updateProductFields(saveProduct, request);

        Optional<Product> foundProduct = productRepo.findByNameIncludingDeleted(request.getName());

        if(foundProduct.isPresent()){
            Product existing = foundProduct.get();

            if(!existing.getDeleted()){
                String message = messagesUtils.getMessage("productservice.product.exist", request.getName());
                log.info(message);
                throw new DuplicateException(message);
            }

            updateProductFields(existing, request);
            existing.setDeleted(false);
            saveProduct = existing;
        }

        return productRepo.save(saveProduct);
    }

    public Product update(UUID id, ProductRequest request) {
        Product existingProduct = productRepo.findById(id)
                .orElseThrow(()-> new IllegalArgumentException(messagesUtils.getMessage("productservice.product.not_found", String.valueOf(id))));

        Optional<Product> foundProduct = productRepo.findByNameIncludingDeleted(request.getName());
        if(foundProduct.isPresent()){
            if(!foundProduct.get().getId().equals(id)){
                String message = messagesUtils.getMessage("productservice.product.exist", request.getName());
                log.info(message);
                throw new DuplicateException(message);
            }
        }

        if(existingProduct.getDeleted()){
            existingProduct.setDeleted(false);
        }

        updateProductFields(existingProduct, request);
        return productRepo.save(existingProduct);
    }

    public void delete(UUID id) {
        Product product = productRepo.findById(id)
                .orElseThrow(()-> new IllegalArgumentException(
                        messagesUtils.getMessage("productservice.product.not_found", String.valueOf(id))));
        product.setIsDeleted(true);
        productRepo.save(product);
    }

    private void updateProductFields(Product target, ProductRequest source) {
        ProductType foundProductType = productTypeService.findByUUID(UUID.fromString(source.getProductTypeId()));

        target.setName(source.getName());
        target.setProductType(foundProductType);
        target.setPrice(source.getPrice());
        target.setCycle(source.getCycle());
        target.setCapacity(source.getCapacity());
        target.setBandwith(source.getBandwith());
        target.setAddon_domain(source.getAddon_domain());
        target.setDatabase_account(source.getDatabase_account());
        target.setFtp_account(source.getFtp_account());
        target.setInfo1(source.getInfo1());
        target.setInfo2(source.getInfo2());
        target.setInfo3(source.getInfo3());
        target.setInfo4(source.getInfo4());
        target.setInfo5(source.getInfo5());
    }

    private ProductDTO convertToProductDTO(Product product) {
        return modelMapper.map(product, ProductDTO.class);
    }

}
