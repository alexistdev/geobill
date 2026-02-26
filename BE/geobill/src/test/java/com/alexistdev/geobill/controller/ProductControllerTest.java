package com.alexistdev.geobill.controller;

import com.alexistdev.geobill.controllers.ProductController;
import com.alexistdev.geobill.dto.ProductDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.request.ProductRequest;
import com.alexistdev.geobill.services.ProductService;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.*;


import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductControllerTest {

    private ProductService productService;

    private ModelMapper modelMapper;

    private ProductController productController;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        modelMapper = mock(ModelMapper.class);
        productController = new ProductController(productService, modelMapper);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get All Products")
    void testGetAllProducts() {
        Product product1 = new Product();
        Product product2= new Product();
        List<Product> productList = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(productList);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        when(productService.getAllProducts(pageable)).thenReturn(productPage);

        ProductDTO productDTO1 = new ProductDTO();
        ProductDTO productDTO2 = new ProductDTO();
        when(modelMapper.map(product1, ProductDTO.class)).thenReturn(productDTO1);
        when(modelMapper.map(product2, ProductDTO.class)).thenReturn(productDTO2);

        ResponseEntity<ResponseData<Page<ProductDTO>>> response =
                productController.getAllProducts(0,10,"id", "asc");

        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody().getPayload());
        Assertions.assertEquals(2, response.getBody().getPayload().getTotalElements());

        verify(productService, times(1)).getAllProducts(pageable);
        verify(modelMapper, times(2)).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Get All Products Fallback - Fallback on Invalid Sort")
    void testGetAllProductFallback() {
        String invalidSort = "nonExistentField";
        Pageable invalidPageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, invalidSort));
        Pageable fallbackPageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id"));

        when(productService.getAllProducts(invalidPageable)).thenThrow(new RuntimeException("Invalid property"));
        when(productService.getAllProducts(fallbackPageable)).thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<ResponseData<Page<ProductDTO>>> response =
                productController.getAllProducts(0, 1, invalidSort, "asc");

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService).getAllProducts(fallbackPageable);
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Get All Products - Empty Result")
    void testGetAllProductEmpty() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        when(productService.getAllProducts(pageable)).thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<ResponseData<Page<ProductDTO>>> response =
                productController.getAllProducts(0, 10, "id", "asc");

        Assertions.assertFalse(response.getBody().isStatus());
        Assertions.assertEquals("No product found", response.getBody().getMessages().get(0));
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Get All Products - Descending Order")
    void testGetAllProductDescending() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        when(productService.getAllProducts(pageable)).thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<ResponseData<Page<ProductDTO>>> response =
                productController.getAllProducts(0, 10, "name", "desc");

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isStatus());
        Assertions.assertEquals("No product found", response.getBody().getMessages().get(0));

        verify(productService).getAllProducts(pageable);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Get All Products - Descending Order With Data")
    void testGetAllProductDescendingWithData() {
        Product product1 = new Product();
        product1.setId(UUID.randomUUID());
        product1.setName("Z-Product");
        Product product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setName("A-Product");
        Page<Product> productPage = new PageImpl<>(List.of(product1, product2));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        when(productService.getAllProducts(pageable)).thenReturn(productPage);

        ProductDTO productDTO1 = new ProductDTO();
        productDTO1.setName("Z-Product");
        ProductDTO productDTO2 = new ProductDTO();
        productDTO2.setName("A-Product");
        when(modelMapper.map(product1, ProductDTO.class)).thenReturn(productDTO1);
        when(modelMapper.map(product2, ProductDTO.class)).thenReturn(productDTO2);

        ResponseEntity<ResponseData<Page<ProductDTO>>> response =
                productController.getAllProducts(0, 10, "name", "desc");

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertEquals("Retrieved page 0 of products", response.getBody().getMessages().get(0));
        Assertions.assertEquals(2, response.getBody().getPayload().getContent().size());
        Assertions.assertEquals("Z-Product", response.getBody().getPayload().getContent().get(0).getName());
        Assertions.assertEquals("A-Product", response.getBody().getPayload().getContent().get(1).getName());

        verify(productService).getAllProducts(pageable);
        verify(modelMapper).map(product1, ProductDTO.class);
        verify(modelMapper).map(product2, ProductDTO.class);
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Search Product")
    void testSearchProduct() {
        Product product1 = new Product();
        product1.setId(UUID.randomUUID());
        product1.setName("Z-Product");
        Product product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setName("A-Product");
        Page<Product> productPage = new PageImpl<>(List.of(product1, product2));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        String filter = "Product";

        when(productService.getAllProductsByFilter(pageable, filter)).thenReturn(productPage);

        ProductDTO productDTO1 = new ProductDTO();
        productDTO1.setName("Z-Product");
        ProductDTO productDTO2 = new ProductDTO();
        productDTO2.setName("A-Product");
        when(modelMapper.map(product1, ProductDTO.class)).thenReturn(productDTO1);
        when(modelMapper.map(product2, ProductDTO.class)).thenReturn(productDTO2);

        ResponseEntity<ResponseData<Page<ProductDTO>>> response =
                productController.searchProduct(filter, 0, 10, "id", "asc");

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertNotNull(response.getBody().getPayload());
        Assertions.assertEquals(2, response.getBody().getPayload().getContent().size());

        verify(productService,times(1)).getAllProductsByFilter(pageable,filter);
        verify(modelMapper, times(2)).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Add Product Success")
    void testAddProductSuccess() {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");

        Product product = new Product();
        product.setName("Test Product");
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Test Product");

        when(productService.save(request)).thenReturn(product);
        when(modelMapper.map(product, ProductDTO.class)).thenReturn(productDTO);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<ResponseData<ProductDTO>> response =
                productController.addProduct(request, bindingResult);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Product created successfully", response.getBody().getMessages().get(0));
        Assertions.assertNotNull(response.getBody().getPayload());
        Assertions.assertEquals("Test Product", response.getBody().getPayload().getName());

        verify(productService, times(1)).save(request);
        verify(modelMapper, times(1)).map(product, ProductDTO.class);
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Add Product Validation Error")
    void testAddProductValidationError() {
        ProductRequest request = new ProductRequest();
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(
                new FieldError("request","name", "Name is required")));

        ResponseEntity<ResponseData<ProductDTO>> response = productController
                .addProduct(request, bindingResult);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Name is required", response.getBody().getMessages().get(0));

        verify(productService, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Add Product Duplicate Exception")
    void testAddProductDuplicateException() {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");

        Product product = new Product();
        product.setName("Test Product");

        when(modelMapper.map(request, Product.class)).thenReturn(product);
        when(productService.save(request)).thenThrow(new DuplicateException("Product already exists"));

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<ResponseData<ProductDTO>> response =
                productController.addProduct(request, bindingResult);

        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Product already exists", response.getBody().getMessages().get(0));

        verify(productService, times(1)).save(request);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Update Product - Success")
    void testUpdateProductSuccess() {
        ProductRequest request = new ProductRequest();
        request.setId(UUID.randomUUID());
        request.setName("Updated Product");

        Product product = new Product();
        product.setId(request.getId());
        product.setName("Updated Product");
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Updated Product");

        when(productService.update(request.getId(),request)).thenReturn(product);
        when(modelMapper.map(product, ProductDTO.class)).thenReturn(productDTO);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<ResponseData<ProductDTO>> response =
                productController.updateProduct(request, bindingResult);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Product updated successfully", response.getBody().getMessages().get(0));
        Assertions.assertNotNull(response.getBody().getPayload());
        Assertions.assertEquals("Updated Product", response.getBody().getPayload().getName());

        verify(productService, times(1)).update(request.getId(),request);
        verify(modelMapper, times(1)).map(product, ProductDTO.class);
    }

    @Test
    @Order(11)
    @DisplayName("11. Test Update Product - Null ID")
    void testUpdateProductNullId() {
        ProductRequest request = new ProductRequest();
        request.setName("Updated Product");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<ResponseData<ProductDTO>> response =
                productController.updateProduct(request, bindingResult);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Product ID is required", response.getBody().getMessages().get(0));

        verify(productService, never()).update(any(), any());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    @Order(12)
    @DisplayName("12. Test Delete Product - Success")
    void testDeleteProductSuccess() {
        UUID uuid = UUID.randomUUID();
        ResponseEntity<ResponseData<Void>> response = productController.deleteProduct(uuid);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertEquals(1, response.getBody().getMessages().size());
        Assertions.assertEquals("Product deleted successfully", response.getBody().getMessages().get(0));

        verify(productService, times(1)).delete(uuid);
    }

    @Test
    @Order(13)
    @DisplayName("13. Test Find Product - By Product ID")
    void testFindProductsByProductTypeID() {
        UUID uuid = UUID.randomUUID();
        Product product = new Product();
        product.setId(uuid);
        product.setName("Test Product");
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Test Product");

        Page<Product> productPage = new PageImpl<>(List.of(product));
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));


        when(productService.getAllProductsByProductTypeId(pageable, uuid)).thenReturn(productPage);
        when(modelMapper.map(product, ProductDTO.class)).thenReturn(productDTO);

        ResponseEntity<ResponseData<Page<ProductDTO>>> response =
                productController.searchProductByType(uuid.toString(), 0,
                        10, "id", "asc");

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isStatus());
        Assertions.assertNotNull(response.getBody().getPayload());
        Assertions.assertEquals(1, response.getBody().getPayload().getContent().size());

        verify(productService,times(1)).getAllProductsByProductTypeId(pageable,uuid);
        verify(modelMapper, times(1)).map(any(Product.class), eq(ProductDTO.class));
    }
}
