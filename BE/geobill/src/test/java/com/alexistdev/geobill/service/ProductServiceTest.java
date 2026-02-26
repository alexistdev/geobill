package com.alexistdev.geobill.service;

import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.repository.ProductRepo;
import com.alexistdev.geobill.request.ProductRequest;
import com.alexistdev.geobill.services.ProductService;
import com.alexistdev.geobill.services.ProductTypeService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductServiceTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private ProductTypeService productTypeService;

    @InjectMocks
    private ProductService productService;

    private ProductRequest productRequest;
    private Product product;
    private UUID productId;
    private ProductType productType;

    @BeforeEach
    void setUp() {
        UUID productTypeId = UUID.randomUUID();
        productType = new ProductType();
        productType.setId(productTypeId);
        productType.setName("Shared Hosting");
        productType.setCreatedBy("system");
        productType.setCreatedDate(new java.util.Date());
        productType.setModifiedBy("system");
        productType.setModifiedDate(new java.util.Date());
        productType.setIsDeleted(false);

        productRequest = new ProductRequest();
        productRequest.setProductTypeId(productTypeId.toString());
        productRequest.setName("Basic Shared Hosting");
        productRequest.setPrice(10.0);
        productRequest.setCycle(1);
        productRequest.setCapacity("10GB");
        productRequest.setBandwith("1000 Mbps");
        productRequest.setAddon_domain("1");
        productRequest.setDatabase_account("1");
        productRequest.setFtp_account("1");

        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setName("Basic Shared Hosting");
        product.setProductType(productType);
        product.setPrice(10.0);
        product.setCycle(1);
        product.setCapacity("10GB");
        product.setBandwith("1000 Mbps");
        product.setAddon_domain("1");
        product.setDatabase_account("1");
        product.setFtp_account("1");
        product.setCreatedBy("system");
        product.setCreatedDate(new java.util.Date());
        product.setModifiedBy("system");
        product.setModifiedDate(new java.util.Date());
        product.setIsDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1:Test Get All Products")
    void testGetAllProducts() {
        List<Product> productList = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(productList);

        when(productRepo.findByIsDeletedFalse(any(Pageable.class))).thenReturn(productPage);

        Page<Product> allProducts = productService.getAllProducts(Pageable.unpaged());

        Assertions.assertNotNull(allProducts);
        Assertions.assertEquals(1, allProducts.getContent().size());
        Assertions.assertEquals(product.getName(), allProducts.getContent().getFirst().getName());
        verify(productRepo, times(1)).findByIsDeletedFalse(any(Pageable.class));
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Save Product - New Product")
    void testSaveProductNew() {
        when(productTypeService.findByUUID(any(UUID.class))).thenReturn(productType);
        when(productRepo.findByNameIncludingDeleted(product.getName())).thenReturn(Optional.empty());
        when(productRepo.save(any(Product.class))).thenReturn(product);

        Product savedProduct = productService.save(productRequest);

        Assertions.assertNotNull(savedProduct);
        Assertions.assertEquals(product.getName(), savedProduct.getName());
        verify(productRepo, times(1)).findByNameIncludingDeleted(product.getName());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @Order(3)
    @DisplayName("3:Test Save Product - Existing Product Not Deleted")
    void testSaveProductExistingNotDeleted() {
        when(productTypeService.findByUUID(any(UUID.class))).thenReturn(productType);
        when(productRepo.findByNameIncludingDeleted(product.getName())).thenReturn(Optional.of(product));

        DuplicateException exception = Assertions.assertThrows(DuplicateException.class,
                () -> productService.save(productRequest));

        Assertions.assertEquals("Product with name 'Basic Shared Hosting' already exists",
                exception.getMessage());
        verify(productRepo, times(1)).findByNameIncludingDeleted(product.getName());
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    @Order(4)
    @DisplayName("4:Test Save Product - Existing Product Deleted")
    void testSaveProductExistingDeleted() {
        product.setIsDeleted(true);
        when(productTypeService.findByUUID(any(UUID.class))).thenReturn(productType);
        when(productRepo.findByNameIncludingDeleted(product.getName())).thenReturn(Optional.of(product));
        when(productRepo.save(any(Product.class))).thenReturn(product);

        Product savedProduct = productService.save(productRequest);

        Assertions.assertNotNull(savedProduct);
        Assertions.assertEquals(product.getName(), savedProduct.getName());
        Assertions.assertFalse(savedProduct.getIsDeleted());
        verify(productRepo, times(1)).findByNameIncludingDeleted(product.getName());
        verify(productRepo, times(1)).save(product);
    }

    @Test
    @Order(5)
    @DisplayName("5:Test Update Product - Existing Product Not Deleted - Successfully updates product")
    void testUpdateProductExistingNotDeleted() {
        ProductRequest request = new ProductRequest();
        request.setName("Updated Basic Shared Hosting");
        UUID newProductTypeId = UUID.randomUUID();
        request.setProductTypeId(newProductTypeId.toString());
        ProductType newProductType = new ProductType();
        newProductType.setId(newProductTypeId);


        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName(request.getName());
        updatedProduct.setProductType(newProductType);

        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.empty());
        when(productTypeService.findByUUID(any(UUID.class))).thenReturn(newProductType);
        when(productRepo.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.update(productId, request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Updated Basic Shared Hosting", result.getName());
        Assertions.assertEquals(newProductType, result.getProductType());
        verify(productRepo, times(1)).findById(productId);
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @Order(6)
    @DisplayName("6:Test Update Product - Existing Product Deleted - Successfully updates and undeletes product")
    void testUpdateProductExistingDeleted() {
        product.setIsDeleted(true);

        ProductRequest request = new ProductRequest();
        request.setName("Updated Basic Shared Hosting");
        UUID newProductTypeId = UUID.randomUUID();
        request.setProductTypeId(newProductTypeId.toString());
        ProductType newProductType = new ProductType();
        newProductType.setId(newProductTypeId);

        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(productRepo.findByNameIncludingDeleted(anyString())).thenReturn(Optional.empty());
        when(productTypeService.findByUUID(newProductTypeId)).thenReturn(newProductType);
        when(productRepo.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Product result = productService.update(productId, request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Updated Basic Shared Hosting", result.getName());
        Assertions.assertFalse(result.getDeleted(), "Product should be undeleted after update");
        verify(productRepo).save(product);
    }

    @Test
    @Order(7)
    @DisplayName("7:Test Update Product - Product Not Found - Throws IllegalArgumentException")
    void testUpdateProductNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("Updated Basic Shared Hosting");
        productRequest.setProductTypeId(UUID.randomUUID().toString());

        when(productRepo.findById(nonExistingId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productService.update(nonExistingId, productRequest);
        });

        Assertions.assertTrue(thrown.getMessage().contains("Product not found with ID:"));
        Assertions.assertTrue(thrown.getMessage().contains(nonExistingId.toString()));

        verify(productRepo, times(1)).findById(nonExistingId);
        verify(productRepo, never()).save(any());
    }

    @Test
    @Order(8)
    @DisplayName("8:Test Delete Product - Successfully delete product")
    void testDeleteProduct() {
        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(productRepo.save(product)).thenReturn(product);

        productService.delete(productId);

        Assertions.assertTrue(product.getDeleted());
        verify(productRepo, times(1)).findById(productId);
        verify(productRepo, times(1)).save(product);
    }

    @Test
    @Order(9)
    @DisplayName("9:Test Delete Product - Product Not Found -Throws IllegalArgumentException")
    void testDeleteProductNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        when(productRepo.findById(nonExistingId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
           productService.delete(nonExistingId);
        });

        Assertions.assertEquals("Product not found with ID:" + nonExistingId, thrown.getMessage());
        verify(productRepo, times(1)).findById(nonExistingId);
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    @Order(10)
    @DisplayName("10:Test Get All Products By Filter")
    void testGetAllProductsByFilter() {
        String keyword = "Shared";
        List<Product> productList = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(productList);

        when(productRepo.findByFilter(eq(keyword.toLowerCase()), any(Pageable.class))).thenReturn(productPage);

        Page<Product> allProducts = productService.getAllProductsByFilter(PageRequest.of(0, 10), keyword);

        Assertions.assertNotNull(allProducts);
        Assertions.assertEquals(1, allProducts.getContent().size());
        Assertions.assertEquals(product.getName(), allProducts.getContent().getFirst().getName());
        verify(productRepo, times(1)).findByFilter(eq(keyword.toLowerCase()),
                any(Pageable.class));
    }

    @Test
    @Order(11)
    @DisplayName("11:Test Get All Products By Product Type UUID")
    void testGetAllProductsByProductTypeId() {
        UUID productTypeId = UUID.randomUUID();
        List<Product> productList = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(productList);

        when(productRepo.findByProductTypeId(eq(productTypeId), any(Pageable.class))).thenReturn(productPage);

        Page<Product> allProducts = productService.getAllProductsByProductTypeId(PageRequest.of(0, 10), productTypeId);

        Assertions.assertNotNull(allProducts);
        Assertions.assertEquals(1, allProducts.getContent().size());
        Assertions.assertEquals(product.getName(), allProducts.getContent().getFirst().getName());
        verify(productRepo, times(1))
                .findByProductTypeId(eq(productTypeId), any(Pageable.class));
    }
}
