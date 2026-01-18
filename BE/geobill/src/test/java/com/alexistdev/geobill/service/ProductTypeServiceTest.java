package com.alexistdev.geobill.service;

import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.repository.ProductTypeRepo;
import com.alexistdev.geobill.services.ProductTypeService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductTypeServiceTest {

    @Mock
    private ProductTypeRepo productTypeRepo;

    @InjectMocks
    private ProductTypeService productTypeService;

    private ProductType productType;
    private UUID productTypeId;

    @BeforeEach
    void setUp() {
        productTypeId = UUID.randomUUID();
        productType = new ProductType();
        productType.setId(productTypeId);
        productType.setName("Shared Hosting");
        productType.setCreatedBy("system");
        productType.setCreatedDate(new java.util.Date());
        productType.setModifiedBy("system");
        productType.setModifiedDate(new java.util.Date());
        productType.setIsDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1:Test Get All ProductTypes")
    void testGetAllProductTypes() {
        List<ProductType> productTypeList = Collections.singletonList(productType);
        Page<ProductType> productTypePage = new PageImpl<>(productTypeList);

        when(productTypeRepo.findByIsDeletedFalse(any(Pageable.class))).thenReturn(productTypePage);

        Page<ProductType> allProductTypes = productTypeService.getAllProductTypes(Pageable.unpaged());

        Assertions.assertNotNull(allProductTypes);
        Assertions.assertEquals(1, allProductTypes.getContent().size());
        Assertions.assertEquals("Shared Hosting", allProductTypes.getContent().getFirst().getName());
        verify(productTypeRepo,times(1)).findByIsDeletedFalse(any(Pageable.class));
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Save ProductType - New ProductType")
    void testSaveProductTypeNew() {
        when(productTypeRepo.findByNameIncludingDeleted(productType.getName())).thenReturn(Optional.empty());
        when(productTypeRepo.save(any(ProductType.class))).thenReturn(productType);

        ProductType savedProductType = productTypeService.save(productType);

        Assertions.assertNotNull(savedProductType);
        Assertions.assertEquals("Shared Hosting", savedProductType.getName());
        verify(productTypeRepo,times(1)).findByNameIncludingDeleted(productType.getName());
        verify(productTypeRepo,times(1)).save(any(ProductType.class));
    }

    @Test
    @Order(3)
    @DisplayName("3:Test Save ProductType - Existing ProductType Not Deleted")
    void testSaveProductTypeExistingNotDeleted() {
        when(productTypeRepo.findByNameIncludingDeleted(productType.getName())).thenReturn(Optional.of(productType));

        DuplicateException exception = Assertions.assertThrows(DuplicateException.class,
                () -> productTypeService.save(productType));

        Assertions.assertEquals("ProductType with name 'Shared Hosting' already exists",
                exception.getMessage());
        verify(productTypeRepo,times(1)).
                findByNameIncludingDeleted(productType.getName());
        verify(productTypeRepo,never()).save(productType);
    }

    @Test
    @Order(4)
    @DisplayName("4:Test Save ProductType - Existing ProductType Deleted")
    void testSaveProductTypeExistingDeleted() {
        productType.setIsDeleted(true);
        when(productTypeRepo.findByNameIncludingDeleted(productType.getName())).thenReturn(Optional.of(productType));
        when(productTypeRepo.save(productType)).thenReturn(productType);

        ProductType savedProductType = productTypeService.save(productType);

        Assertions.assertNotNull(savedProductType);
        Assertions.assertEquals("Shared Hosting", savedProductType.getName());
        Assertions.assertFalse(savedProductType.getIsDeleted());
        verify(productTypeRepo,times(1)).findByNameIncludingDeleted(productType.getName());
        verify(productTypeRepo,times(1)).save(productType);
    }

    @Test
    @Order(5)
    @DisplayName("5:Test Update ProductType - Existing ProductType Not Deleted")
    void testUpdateProductTypeExistingNotDeleted() {
        ProductType updatedProductType = new ProductType();
        updatedProductType.setId(productTypeId);
        updatedProductType.setName("Updated VPS");
        updatedProductType.setDeleted(false);

        when(productTypeRepo.findById(productTypeId)).thenReturn(Optional.of(productType));
        when(productTypeRepo.save(any(ProductType.class))).thenReturn(updatedProductType);

        ProductType result = productTypeService.update(productTypeId, updatedProductType);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Updated VPS", result.getName());
        verify(productTypeRepo, times(1)).findById(productTypeId);
        verify(productTypeRepo, times(1)).save(any(ProductType.class));
    }

    @Test
    @Order(6)
    @DisplayName("6:Test Update ProductType - Existing ProductType Deleted")
    void testUpdateProductTypeExistingDeleted() {
        productType.setDeleted(true);
        ProductType updatedProductType = new ProductType();
        updatedProductType.setId(productTypeId);
        updatedProductType.setName("Updated VPS");
        updatedProductType.setDeleted(false);

        when(productTypeRepo.findById(productTypeId)).thenReturn(Optional.of(productType));
        when(productTypeRepo.save(any(ProductType.class))).thenReturn(productType);

        ProductType result = productTypeService.update(productTypeId, updatedProductType);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Updated VPS", result.getName());
        Assertions.assertFalse(result.getDeleted());
        verify(productTypeRepo, times(1)).findById(productTypeId);
        verify(productTypeRepo, times(1)).save(any(ProductType.class));
    }

    @Test
    @Order(7)
    @DisplayName("7:Test Update ProductType - ProductType Not Found")
    void testUpdateProductTypeNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        ProductType updatedProductType = new ProductType();
        updatedProductType.setId(nonExistingId);
        updatedProductType.setName("Updated VPS");
        updatedProductType.setDeleted(false);

        when(productTypeRepo.findById(nonExistingId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productTypeService.update(nonExistingId, updatedProductType);
        });

        Assertions.assertEquals("ProductType not found with ID: " + nonExistingId, thrown.getMessage());
        verify(productTypeRepo, times(1)).findById(nonExistingId);
        verify(productTypeRepo, never()).save(any(ProductType.class));
    }

    @Test
    @Order(8)
    @DisplayName("8:Test Delete ProductType")
    void testDeleteProductType() {
        when(productTypeRepo.findById(productTypeId)).thenReturn(Optional.of(productType));
        when(productTypeRepo.save(productType)).thenReturn(productType);

        productTypeService.delete(productTypeId);

        Assertions.assertTrue(productType.getDeleted());
        verify(productTypeRepo, times(1)).findById(productTypeId);
        verify(productTypeRepo, times(1)).save(productType);
    }

    @Test
    @Order(9)
    @DisplayName("9:Test Delete ProductType - ProductType Not Found")
    void testDeleteProductTypeNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        when(productTypeRepo.findById(nonExistingId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productTypeService.delete(nonExistingId);
        });

        Assertions.assertEquals("ProductType not found with ID: " + nonExistingId, thrown.getMessage());
        verify(productTypeRepo, times(1)).findById(nonExistingId);
        verify(productTypeRepo, never()).save(any(ProductType.class));
    }
}
