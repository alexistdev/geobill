package com.alexistdev.geobill.seeder.product;

import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.repository.ProductTypeRepo;
import com.alexistdev.geobill.seeder.SeedAudit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductTypeSeederTest {

    @Mock
    private ProductTypeRepo productTypeRepo;

    @InjectMocks
    private ProductTypeSeeder productTypeSeeder;

    @SuppressWarnings("unchecked")
    private List<ProductType> captureSavedProductTypes() {
        ArgumentCaptor<List<ProductType>> captor = ArgumentCaptor.forClass(List.class);
        verify(productTypeRepo, times(1)).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    @Order(1)
    @DisplayName("1:Test Seeder Name And Order")
    void testNameAndOrder() {
        Assertions.assertEquals("product types", productTypeSeeder.name());
        Assertions.assertEquals(30, productTypeSeeder.order());
    }

    @Test
    @Order(2)
    @DisplayName("2:Test ShouldRun Is True When The Table Is Empty")
    void testShouldRunOnEmptyTable() {
        when(productTypeRepo.count()).thenReturn(0L);

        Assertions.assertTrue(productTypeSeeder.shouldRun());
        verify(productTypeRepo, times(1)).count();
    }

    @Test
    @Order(3)
    @DisplayName("3:Test ShouldRun Is False When Product Types Already Exist")
    void testShouldNotRunWhenProductTypesExist() {
        when(productTypeRepo.count()).thenReturn(2L);

        Assertions.assertFalse(productTypeSeeder.shouldRun());
        verify(productTypeRepo, times(1)).count();
    }

    @Test
    @Order(4)
    @DisplayName("4:Test Seed Saves Every Catalog Product Type")
    void testSeedSavesEveryProductType() {
        productTypeSeeder.seed();

        List<ProductType> saved = captureSavedProductTypes();

        Assertions.assertEquals(ProductCatalog.productTypes().size(), saved.size());
        Assertions.assertEquals(ProductCatalog.SHARED_HOSTING, saved.get(0).getName());
        Assertions.assertEquals(ProductCatalog.VPS, saved.get(1).getName());
    }

    @Test
    @Order(5)
    @DisplayName("5:Test Seeded Product Types Carry The Audit Columns")
    void testSeededProductTypesAreStamped() {
        productTypeSeeder.seed();

        for (ProductType productType : captureSavedProductTypes()) {
            Assertions.assertEquals(SeedAudit.SYSTEM_USER, productType.getCreatedBy());
            Assertions.assertEquals(SeedAudit.SYSTEM_USER, productType.getModifiedBy());
            Assertions.assertNotNull(productType.getCreatedDate());
            Assertions.assertNotNull(productType.getModifiedDate());
            Assertions.assertFalse(productType.getDeleted());
        }
    }
}
