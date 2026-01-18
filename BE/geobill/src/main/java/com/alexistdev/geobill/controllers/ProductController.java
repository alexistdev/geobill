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
    private final ProductTypeService productTypeService;
    private final ModelMapper modelMapper;

    public ProductController(ProductService productService, ModelMapper modelMapper,ProductTypeService productTypeService) {
        this.productService = productService;
        this.modelMapper = modelMapper;
        this.productTypeService = productTypeService;
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

        handleNonEmptyPage(responseData, productPage, page);
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
        handleNonEmptyPage(responseData, productPage, page);
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
            Product result = convertToProduct(request);
            responseData.getMessages().add("Product successfully added");
            responseData.setPayload(toDTO(productService.save(result)));
            responseData.setStatus(true);
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

    private Product convertToProduct(ProductRequest request){

        ProductType foundProductType = productTypeService.findByUUID(UUID.fromString(request.getProductTypeId()));
        Product created = new Product();
        created.setProductType(foundProductType);
        created.setName(request.getName());
        created.setPrice(request.getPrice());
        created.setCycle(request.getCycle());
        created.setCapacity(request.getCapacity());
        created.setBandwith(request.getBandwith());
        created.setAddon_domain(request.getAddon_domain());
        created.setDatabase_account(request.getDatabase_account());
        created.setFtp_account(request.getFtp_account());
        created.setInfo1(request.getInfo1());
        created.setInfo2(request.getInfo2());
        created.setInfo3(request.getInfo3());
        created.setInfo4(request.getInfo4());
        created.setInfo5(request.getInfo5());
        return created;
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = modelMapper.map(product, ProductDTO.class);
        if (product.getProductType() != null) {
            dto.setProductTypeId(product.getProductType().getId().toString());
        }
        return dto;
    }

    private void handleNonEmptyPage(ResponseData<Page<ProductDTO>> responseData, Page<Product> pageResult, int pageNumber) {
        if (!pageResult.isEmpty()) {
            responseData.setStatus(true);
            if (!responseData.getMessages().isEmpty()) {
                responseData.getMessages().removeFirst();
            }
            responseData.getMessages().add("Retrieved page " + pageNumber + " of products");

            Page<ProductDTO> productDTOPage = pageResult.map(product -> {
                ProductDTO dto = toDTO(product);
                return dto;
            });

            responseData.setPayload(productDTOPage);
        }
    }

    private void processErrors(Errors errors, ResponseData<?> responseData) {
        for (ObjectError error : errors.getAllErrors()) {
            responseData.getMessages().add(error.getDefaultMessage());
        }
    }

}
