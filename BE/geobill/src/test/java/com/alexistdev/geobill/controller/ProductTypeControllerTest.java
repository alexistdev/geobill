package com.alexistdev.geobill.controller;

import com.alexistdev.geobill.controllers.ProductTypeController;
import com.alexistdev.geobill.dto.ProductTypeDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.services.ProductTypeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class ProductTypeControllerTest {

    private ProductTypeService productTypeService;

    private ModelMapper modelMapper;

    private ProductTypeController productTypeController;

    @BeforeEach
    void setUp() {
        productTypeService = mock(ProductTypeService.class);
        modelMapper = mock(ModelMapper.class);
        productTypeController = new ProductTypeController(productTypeService, modelMapper);
    }

    @Test
    @DisplayName("Test Get All Product Types")
    void testGetAllProductTypes() {
        ProductType productType1 = new ProductType();
        ProductType productType2= new ProductType();
        List<ProductType> productTypeList = Arrays.asList(productType1, productType2);
        Page<ProductType> productTypePage = new PageImpl<>(productTypeList);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        when(productTypeService.getAllProductTypes(pageable)).thenReturn(productTypePage);

        ProductTypeDTO productTypeDTO1 = new ProductTypeDTO();
        ProductTypeDTO productTypeDTO2 = new ProductTypeDTO();
        when(modelMapper.map(productType1, ProductTypeDTO.class)).thenReturn(productTypeDTO1);
        when(modelMapper.map(productType2, ProductTypeDTO.class)).thenReturn(productTypeDTO2);

        ResponseEntity<ResponseData<Page<ProductTypeDTO>>> response =
                productTypeController.getAllProductTypes(0,10,"id", "asc");

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertNotNull(response.getBody().getPayload());
        Assertions.assertEquals(2, response.getBody().getPayload().getTotalElements());

        verify(productTypeService, times(1)).getAllProductTypes(pageable);
        verify(modelMapper, times(2)).map(any(ProductType.class), eq(ProductTypeDTO.class));
    }

    @Test
    @DisplayName("Test Get All Product Types Fallback - Fallback on Invalid Sort")
    void testGetAllProductTypesFallback() {
        String invalidSort = "nonExistentField";
        Pageable invalidPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, invalidSort));
        Pageable fallbackPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

        when(productTypeService.getAllProductTypes(invalidPageable)).thenThrow(new RuntimeException("Invalid property"));
        when(productTypeService.getAllProductTypes(fallbackPageable)).thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<ResponseData<Page<ProductTypeDTO>>> response =
                productTypeController.getAllProductTypes(0, 10, invalidSort, "asc");

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productTypeService).getAllProductTypes(fallbackPageable);
    }

    @Test
    @DisplayName("Test Get All Product Types - Empty Result")
    void testGetAllProductTypesEmpty() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        when(productTypeService.getAllProductTypes(pageable)).thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<ResponseData<Page<ProductTypeDTO>>> response =
                productTypeController.getAllProductTypes(0, 10, "id", "asc");

        Assertions.assertFalse(response.getBody().isStatus());
        Assertions.assertEquals("No product type found", response.getBody().getMessages().get(0));
    }

    @Test
    @DisplayName("Test Get All Product Types - Descending Order")
    void testGetAllProductTypesDescending() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        when(productTypeService.getAllProductTypes(pageable)).thenReturn(new PageImpl<>(List.of()));

        productTypeController.getAllProductTypes(0, 10, "name", "desc");

        verify(productTypeService).getAllProductTypes(pageable);
    }
}
