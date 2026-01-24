package com.alexistdev.geobill.controller;

import com.alexistdev.geobill.controllers.ProductTypeController;
import com.alexistdev.geobill.dto.ProductTypeDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.request.ProductTypeRequest;
import com.alexistdev.geobill.services.ProductTypeService;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    @Order(1)
    @DisplayName("1. Test Get All Product Types")
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
    @Order(2)
    @DisplayName("2. Test Get All Product Types Fallback - Fallback on Invalid Sort")
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
    @Order(3)
    @DisplayName("3. Test Get All Product Types - Empty Result")
    void testGetAllProductTypesEmpty() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        when(productTypeService.getAllProductTypes(pageable)).thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<ResponseData<Page<ProductTypeDTO>>> response =
                productTypeController.getAllProductTypes(0, 10, "id", "asc");

        Assertions.assertFalse(response.getBody().isStatus());
        Assertions.assertEquals("No product type found", response.getBody().getMessages().get(0));
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Get All Product Types - Descending Order")
    void testGetAllProductTypesDescending() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        when(productTypeService.getAllProductTypes(pageable)).thenReturn(new PageImpl<>(List.of()));

        productTypeController.getAllProductTypes(0, 10, "name", "desc");

        verify(productTypeService).getAllProductTypes(pageable);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Search Product Types")
    void testSearchProductTypes() {
        ProductType productType1 = new ProductType();
        productType1.setName("Product Type 1");
        ProductType productType2= new ProductType();
        productType2.setName("Product Type 2");
        List<ProductType> productTypeList = Arrays.asList(productType1, productType2);
        Page<ProductType> productTypePage = new PageImpl<>(productTypeList);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        String filter = "Product";
        when(productTypeService.getAllProductTypesByFilter(pageable, filter)).thenReturn(productTypePage);

        ProductTypeDTO productTypeDTO1 = new ProductTypeDTO();
        productTypeDTO1.setName("Product Type 1");
        ProductTypeDTO productTypeDTO2 = new ProductTypeDTO();
        productTypeDTO2.setName("Product Type 2");
        when(modelMapper.map(productType1, ProductTypeDTO.class)).thenReturn(productTypeDTO1);
        when(modelMapper.map(productType2, ProductTypeDTO.class)).thenReturn(productTypeDTO2);

        ResponseEntity<ResponseData<Page<ProductTypeDTO>>> response =
                productTypeController.searchProductTypes(filter,0,10,"id","asc");

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertNotNull(response.getBody().getPayload());
        Assertions.assertEquals(2, response.getBody().getPayload().getTotalElements());

        verify(productTypeService, times(1)).getAllProductTypesByFilter(pageable, filter);
        verify(modelMapper, times(2)).map(any(ProductType.class), eq(ProductTypeDTO.class));
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Add Product Type Success")
    void testAddProductTypeSuccess() throws Exception {
        ProductTypeRequest request = new ProductTypeRequest();
        request.setName("Test Product Type");

        ProductType productType = new ProductType();
        productType.setName("Test Product Type");
        ProductTypeDTO productTypeDTO = new ProductTypeDTO();
        productTypeDTO.setName("Test Product Type");

        when(modelMapper.map(request, ProductType.class)).thenReturn(productType);
        when(productTypeService.save(productType)).thenReturn(productType);
        when(modelMapper.map(productType, ProductTypeDTO.class)).thenReturn(productTypeDTO);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<ResponseData<ProductTypeDTO>> response =
                productTypeController.addProductType(request, bindingResult);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Product Type successfully added", response.getBody().getMessages().get(0));
        Assertions.assertNotNull(response.getBody().getPayload());
        Assertions.assertEquals("Test Product Type", response.getBody().getPayload().getName());

        verify(productTypeService, times(1)).save(productType);
        verify(modelMapper, times(1)).map(request, ProductType.class);
        verify(modelMapper, times(1)).map(productType, ProductTypeDTO.class);
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Add Product Type Validation Error")
    void testAddProductTypeValidationError() {
        ProductTypeRequest request = new ProductTypeRequest();
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(Collections
                .singletonList(
                        new FieldError("request", "name", "Name is required")));

        ResponseEntity<ResponseData<ProductTypeDTO>> response = productTypeController
                .addProductType(request, bindingResult);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Name is required", response.getBody().getMessages().get(0));
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Add Product Type Duplicate Exception")
    void testAddProductTypeDuplicateException() throws Exception {
        ProductTypeRequest request = new ProductTypeRequest();
        request.setName("Test Product Type");

        ProductType productType = new ProductType();
        productType.setName("Test Product Type");

        when(modelMapper.map(request, ProductType.class)).thenReturn(productType);
        when(productTypeService.save(productType)).thenThrow(new DuplicateException("Product Type already exists"));

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<ResponseData<ProductTypeDTO>> response =
                productTypeController.addProductType(request, bindingResult);

        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Product Type already exists", response.getBody().getMessages().get(0));
    }

    @Test
    @DisplayName("9. Test Update Product Type - Success")
    void testUpdateProductTypeSuccess() {
        ProductTypeRequest request = new ProductTypeRequest();
        request.setId(UUID.randomUUID());
        request.setName("Updated Product Type");

        ProductType productType = new ProductType();
        productType.setId(request.getId());
        productType.setName("Updated Product Type");
        ProductTypeDTO productTypeDTO = new ProductTypeDTO();
        productTypeDTO.setName("Updated Product Type");

        when(modelMapper.map(request, ProductType.class)).thenReturn(productType);
        when(productTypeService.update(request.getId(),productType)).thenReturn(productType);
        when(modelMapper.map(productType, ProductTypeDTO.class)).thenReturn(productTypeDTO);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<ResponseData<ProductTypeDTO>> response =
                productTypeController.updateProductType(request, bindingResult);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Product Type successfully added", response.getBody().getMessages().get(0));
        Assertions.assertNotNull(response.getBody().getPayload());
        Assertions.assertEquals("Updated Product Type", response.getBody().getPayload().getName());

        verify(productTypeService, times(1)).update(request.getId(),productType);
        verify(modelMapper, times(1)).map(request, ProductType.class);
        verify(modelMapper, times(1)).map(productType, ProductTypeDTO.class);
    }

    @Test
    @DisplayName("10. Test Update Product Type - Null ID")
    void testUpdateProductTypeNullId() {
        ProductTypeRequest request = new ProductTypeRequest();
        request.setName("Updated Product Type");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<ResponseData<ProductTypeDTO>> response =
                productTypeController.updateProductType(request, bindingResult);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Product Type ID is required", response.getBody().getMessages().get(0));

        verify(productTypeService, never()).update(any(), any());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    @DisplayName("11. Test Delete Product Type - Success")
    void testDeleteProductTypeSuccess() throws Exception {
        UUID uuid = UUID.randomUUID();
        ResponseEntity<ResponseData<Void>> response = productTypeController.deleteProductType(uuid);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Product Type has been deleted", response.getBody().getMessages().get(0));

        verify(productTypeService, times(1)).delete(uuid);
    }

    @Test
    @DisplayName("12. Test Get All Product Types - Size 0")
    void testGetAllProductTypesSizeZero() {
        ProductType productType1 = new ProductType();
        ProductType productType2= new ProductType();

        List<ProductType> productTypeList = Arrays.asList(productType1, productType2);
        Page<ProductType> productTypePage = new PageImpl<>(productTypeList);

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
        when(productTypeService.getAllProductTypes(pageable)).thenReturn(productTypePage);

        ProductTypeDTO productTypeDTO1 = new ProductTypeDTO();
        ProductTypeDTO productTypeDTO2 = new ProductTypeDTO();
        when(modelMapper.map(productType1, ProductTypeDTO.class)).thenReturn(productTypeDTO1);
        when(modelMapper.map(productType2, ProductTypeDTO.class)).thenReturn(productTypeDTO2);

        ResponseEntity<ResponseData<Page<ProductTypeDTO>>> response =
                productTypeController.getAllProductTypes(0,0,"id", "asc");

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertNotNull(response.getBody().getPayload());
        Assertions.assertEquals(2, response.getBody().getPayload().getTotalElements());

        verify(productTypeService, times(1)).getAllProductTypes(pageable);
        verify(modelMapper, times(2)).map(any(ProductType.class), eq(ProductTypeDTO.class));
    }
}
