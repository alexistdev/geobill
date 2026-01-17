package com.alexistdev.geobill.controllers;

import com.alexistdev.geobill.config.ValidationConstant;
import com.alexistdev.geobill.dto.ProductTypeDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.request.ProductTypeRequest;
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
@RequestMapping("/api/v1/producttypes")
public class ProductTypeController {

    private static final String NO_PRODUCT_TYPE_FOUND = "No product type found";

    private final ProductTypeService productTypeService;
    private final ModelMapper modelMapper;


    public ProductTypeController(ProductTypeService productTypeService, ModelMapper modelMapper) {
        this.productTypeService = productTypeService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<ResponseData<Page<ProductTypeDTO>>> getAllProductTypes(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        ResponseData<Page<ProductTypeDTO>> responseData = new ResponseData<>();

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ProductType> productTypePage;
        try {
            productTypePage = productTypeService.getAllProductTypes(pageable);
        } catch (RuntimeException e){
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            productTypePage = productTypeService.getAllProductTypes(fallbackPageable);
        }

        responseData.getMessages().add(NO_PRODUCT_TYPE_FOUND);
        responseData.setStatus(false);

        handleNonEmptyPage(responseData,productTypePage,page);

        Page<ProductTypeDTO> productTypeDTOS = productTypePage
                .map(productType
                        -> modelMapper.map(productType, ProductTypeDTO.class));
        responseData.setPayload(productTypeDTOS);

        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<ProductTypeDTO>>> searchProductTypes(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        ResponseData<Page<ProductTypeDTO>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ProductType> productTypePage;
        try {
            productTypePage = productTypeService.getAllProductTypesByFilter(pageable, filter);
        } catch (RuntimeException e){
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            productTypePage = productTypeService.getAllProductTypesByFilter(fallbackPageable, filter);
        }
        responseData.getMessages().add(NO_PRODUCT_TYPE_FOUND);
        responseData.setStatus(false);

        if(!productTypePage.isEmpty()){
            responseData.setStatus(true);
            responseData.getMessages().removeFirst();
            responseData.getMessages().add("Retrieved page " + page + " of product types");
        }
        Page<ProductTypeDTO> productTypeDTOS = productTypePage.map(productType -> modelMapper.map(productType, ProductTypeDTO.class));
        responseData.setPayload(productTypeDTOS);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping
    public ResponseEntity<ResponseData<ProductTypeDTO>> addProductType(@Valid @RequestBody ProductTypeRequest request, Errors erros) {
        ResponseData<ProductTypeDTO> responseData = new ResponseData<>();
        responseData.setStatus(false);
        if(erros.hasErrors()){
            processErrors(erros, responseData);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        try {
            ProductType result = productTypeService.save(modelMapper.map(request, ProductType.class));
            responseData.getMessages().add(ValidationConstant.success("Product Type"));
            responseData.setPayload(modelMapper.map(result, ProductTypeDTO.class));
            responseData.setStatus(true);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
        } catch (DuplicateException d){
            log.error("Error creating Product Type", d);
            responseData.getMessages().add(d.getMessage());
            responseData.setPayload(null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseData);
        } catch (Exception e) {
            log.error("Error creating Product Type", e);
            responseData.getMessages().add("Error :" + e.getMessage());
            responseData.setPayload(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    @PatchMapping
    public ResponseEntity<ResponseData<ProductTypeDTO>> updateProductType(@Valid @RequestBody ProductTypeRequest request, Errors erros) {
        ResponseData<ProductTypeDTO> responseData = new ResponseData<>();
        responseData.setStatus(false);
        if(request.getId() == null){
            responseData.getMessages().add("Product Type ID is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        if(erros.hasErrors()){
            processErrors(erros, responseData);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        try {
            ProductType result = productTypeService.update(request.getId(), modelMapper.map(request, ProductType.class));
            responseData.getMessages().add(ValidationConstant.success("Product Type"));
            responseData.setPayload(modelMapper.map(result, ProductTypeDTO.class));
            responseData.setStatus(true);
            return ResponseEntity.status(HttpStatus.OK).body(responseData);
        } catch (Exception e) {
            log.error("Error updating Product Type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteProductType(@PathVariable("id") UUID uuid){
        ResponseData<Void> responseData = new ResponseData<>();
        responseData.setStatus(false);
        try {
            productTypeService.delete(uuid);
            responseData.getMessages().add(ValidationConstant.deleted("Product Type"));
            responseData.setStatus(true);
            return ResponseEntity.status(HttpStatus.OK).body(responseData);
        } catch (Exception e) {
            log.error("Error deleting Product Type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    private <T> void handleNonEmptyPage(ResponseData<Page<T>> responseData, Page<?> pageResult, int pageNumber){
        if(!pageResult.isEmpty()){
            responseData.setStatus(true);
            if(!responseData.getMessages().isEmpty()){
                responseData.getMessages().removeFirst();
            }
            responseData.getMessages().add("Retrieved page " + pageNumber + " of product types");
        }
    }

    private void processErrors(Errors errors, ResponseData<?> responseData) {
        for (ObjectError error : errors.getAllErrors()) {
            responseData.getMessages().add(error.getDefaultMessage());
        }
    }
}
