package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.dto.ProductDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.request.ProductRequest;
import com.alexistdev.geobill.services.ProductService;
import com.alexistdev.geobill.services.ProductTypeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private static final String NO_PRODUCT_FOUND = "No product found";

    private final ProductService productService;
    private final ModelMapper modelMapper;

    public ProductController(ProductService productService, ModelMapper modelMapper) {
        this.productService = productService;
        this.modelMapper = modelMapper;
    }



    @GetMapping
    public ResponseEntity<ResponseData<Page<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        ResponseData<Page<ProductDTO>> responseData = new ResponseData<>();

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Product> productPage;
        try {
            productPage = productService.getAllProducts(pageable);
        } catch (RuntimeException e){
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            productPage = productService.getAllProducts(fallbackPageable);
        }

        responseData.getMessages().add(NO_PRODUCT_FOUND);
        responseData.setStatus(false);

        handleNonEmptyPage(responseData,productPage,page);

        Page<ProductDTO> productDTOPage = productPage.map(product -> modelMapper.map(product, ProductDTO.class));
        responseData.setPayload(productDTOPage);

        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<ProductDTO>>> searchProduct(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        ResponseData<Page<ProductDTO>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Product> productPage;
        try {
            productPage = productService.getAllProductsByFilter(pageable, filter);
        } catch (RuntimeException e){
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            productPage = productService.getAllProductsByFilter(fallbackPageable, filter);
        }
        responseData.getMessages().add(NO_PRODUCT_FOUND);
        responseData.setStatus(false);

        if (!productPage.isEmpty()) {
            responseData.setStatus(true);
            if (!responseData.getMessages().isEmpty()) {
                responseData.getMessages().removeFirst();
            }
            responseData.getMessages().add("Retrieved page " + page + " of products");
        }
        Page<ProductDTO> productDTOPage = productPage.map(product -> modelMapper.map(product, ProductDTO.class));
        responseData.setPayload(productDTOPage);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @GetMapping("/search-by-type")
    public ResponseEntity<ResponseData<Page<ProductDTO>>> searchProductByType(
            @RequestParam(defaultValue = "") String id,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        ResponseData<Page<ProductDTO>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Product> productPage;
        try {
            productPage = productService.getAllProductsByProductTypeId(pageable, UUID.fromString(id));
        } catch (RuntimeException e){
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            productPage = productService.getAllProductsByProductTypeId(fallbackPageable, UUID.fromString(id));
        }
        responseData.getMessages().add(NO_PRODUCT_FOUND);
        responseData.setStatus(false);

        if (!productPage.isEmpty()) {
            responseData.setStatus(true);
            if (!responseData.getMessages().isEmpty()) {
                responseData.getMessages().removeFirst();
            }
            responseData.getMessages().add("Retrieved page " + page + " of products");
        }
        Page<ProductDTO> productDTOPage = productPage.map(product -> modelMapper.map(product, ProductDTO.class));
        responseData.setPayload(productDTOPage);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping
    public ResponseEntity<ResponseData<ProductDTO>> addProduct(@Valid @RequestBody ProductRequest request, Errors errors) {
        ResponseData<ProductDTO> responseData = new ResponseData<>();
        responseData.setStatus(false);
        if(errors.hasErrors()){
            processErrors(errors, responseData);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        try {
            Product created = productService.save(request);
            responseData.setStatus(true);
            responseData.getMessages().add("Product created successfully");
            responseData.setPayload(modelMapper.map(created, ProductDTO.class));
            return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
        } catch (DuplicateException d){
            log.error("Error creating Product", d);
            responseData.getMessages().add(d.getMessage());
            responseData.setPayload(null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseData);
        } catch (Exception e){
            log.error("Error creating Product", e);
            responseData.getMessages().add("Error :" + e.getMessage());
            responseData.setPayload(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    @PatchMapping
    public ResponseEntity<ResponseData<ProductDTO>> updateProduct(@Valid @RequestBody ProductRequest request, Errors errors) {
        ResponseData<ProductDTO> responseData = new ResponseData<>();
        responseData.setStatus(false);
        responseData.setPayload(null);
        if(request.getId() == null){
            responseData.getMessages().add("Product ID is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        if(errors.hasErrors()) {
            processErrors(errors, responseData);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        try {
            Product result = productService.update(request.getId(), request);
            responseData.setStatus(true);
            responseData.getMessages().add("Product updated successfully");
            responseData.setPayload(modelMapper.map(result, ProductDTO.class));
            return ResponseEntity.status(HttpStatus.OK).body(responseData);
        } catch (DuplicateException d) {
            log.error("Error updating Product", d);
            responseData.getMessages().add(d.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseData);
        } catch (Exception e) {
            log.error("Error updating Product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteProduct(@PathVariable("id") UUID uuid){
        ResponseData<Void> responseData = new ResponseData<>();
        responseData.setStatus(false);
        try {
            productService.delete(uuid);
            responseData.setStatus(true);
            responseData.getMessages().add("Product deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body(responseData);
        } catch (Exception e) {
            log.error("Error deleting Product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    private void processErrors(Errors errors, ResponseData<?> responseData) {
        for (ObjectError error : errors.getAllErrors()) {
            responseData.getMessages().add(error.getDefaultMessage());
        }
    }

    private <T> void handleNonEmptyPage(ResponseData<Page<T>> responseData, Page<?> pageResult, int pageNumber){
        if(!pageResult.isEmpty()){
            responseData.setStatus(true);
            if(!responseData.getMessages().isEmpty()){
                responseData.getMessages().removeFirst();
            }
            responseData.getMessages().add("Retrieved page " + pageNumber + " of products");
        }
    }

}
