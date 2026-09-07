package com.alexistdev.geobill.seeder.product;

import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.repository.ProductRepo;
import com.alexistdev.geobill.models.repository.ProductTypeRepo;
import com.alexistdev.geobill.seeder.SeedAudit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductSeederTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private ProductTypeRepo productTypeRepo;

    @InjectMocks
    private ProductSeeder productSeeder;

    private ProductType sharedHosting;

    @BeforeEach
    void setUp() {
        sharedHosting = new ProductType();
        sharedHosting.setId(UUID.randomUUID());
        sharedHosting.setName(ProductCatalog.SHARED_HOSTING);
    }

    @SuppressWarnings("unchecked")
    private List<Product> captureSavedProducts() {
        ArgumentCaptor<List<Product>> captor = ArgumentCaptor.forClass(List.class);
        verify(productRepo, times(1)).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    @Order(1)
    @DisplayName("1:Test Seeder Name And Order")
    void testNameAndOrder() {
        Assertions.assertEquals("products", productSeeder.name());
        Assertions.assertEquals(40, productSeeder.order());
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Product Seeder Runs After The Product Type Seeder")
    void testRunsAfterProductTypeSeeder() {
        Assertions.assertTrue(productSeeder.order() > new ProductTypeSeeder(productTypeRepo).order());
    }

    @Test
    @Order(3)
    @DisplayName("3:Test ShouldRun Is True When The Table Is Empty")
    void testShouldRunOnEmptyTable() {
        when(productRepo.count()).thenReturn(0L);

        Assertions.assertTrue(productSeeder.shouldRun());
        verify(productRepo, times(1)).count();
    }

    @Test
    @Order(4)
    @DisplayName("4:Test ShouldRun Is False When Products Already Exist")
    void testShouldNotRunWhenProductsExist() {
        when(productRepo.count()).thenReturn(3L);

        Assertions.assertFalse(productSeeder.shouldRun());
        verify(productRepo, times(1)).count();
    }

    @Test
    @Order(5)
    @DisplayName("5:Test Seed Saves Every Catalog Product")
    void testSeedSavesEveryProduct() {
        when(productTypeRepo.findByNameIncludingDeleted(ProductCatalog.SHARED_HOSTING))
                .thenReturn(Optional.of(sharedHosting));

        productSeeder.seed();

        List<Product> saved = captureSavedProducts();

        Assertions.assertEquals(ProductCatalog.products().size(), saved.size());
        Assertions.assertEquals("NVME-1", saved.get(0).getName());
        Assertions.assertEquals("NVME-2", saved.get(1).getName());
        Assertions.assertEquals("NVME-3", saved.get(2).getName());
    }

    @Test
    @Order(6)
    @DisplayName("6:Test Seed Maps Every Catalog Value Onto The Product")
    void testSeedMapsEveryValue() {
        when(productTypeRepo.findByNameIncludingDeleted(ProductCatalog.SHARED_HOSTING))
                .thenReturn(Optional.of(sharedHosting));

        productSeeder.seed();

        Product product = captureSavedProducts().getFirst();
        ProductDefinition definition = ProductCatalog.products().getFirst();

        Assertions.assertEquals(definition.name(), product.getName());
        Assertions.assertEquals(sharedHosting, product.getProductType());
        Assertions.assertEquals(definition.price(), product.getPrice());
        Assertions.assertEquals(definition.cycle(), product.getCycle());
        Assertions.assertEquals(definition.capacity(), product.getCapacity());
        Assertions.assertEquals(definition.bandwith(), product.getBandwith());
        Assertions.assertEquals(definition.addonDomain(), product.getAddon_domain());
        Assertions.assertEquals(definition.databaseAccount(), product.getDatabase_account());
        Assertions.assertEquals(definition.ftpAccount(), product.getFtp_account());
        Assertions.assertEquals(definition.info1(), product.getInfo1());
        Assertions.assertEquals(definition.info2(), product.getInfo2());
        Assertions.assertEquals(definition.info3(), product.getInfo3());
        Assertions.assertNull(product.getInfo4());
        Assertions.assertNull(product.getInfo5());
    }

    @Test
    @Order(7)
    @DisplayName("7:Test Seeded Products Carry The Audit Columns")
    void testSeededProductsAreStamped() {
        when(productTypeRepo.findByNameIncludingDeleted(ProductCatalog.SHARED_HOSTING))
                .thenReturn(Optional.of(sharedHosting));

        productSeeder.seed();

        for (Product product : captureSavedProducts()) {
            Assertions.assertEquals(SeedAudit.SYSTEM_USER, product.getCreatedBy());
            Assertions.assertEquals(SeedAudit.SYSTEM_USER, product.getModifiedBy());
            Assertions.assertNotNull(product.getCreatedDate());
            Assertions.assertNotNull(product.getModifiedDate());
            Assertions.assertFalse(product.getDeleted());
        }
    }

    @Test
    @Order(8)
    @DisplayName("8:Test Seed Fails When The Product Type Is Unknown")
    void testSeedFailsOnUnknownProductType() {
        when(productTypeRepo.findByNameIncludingDeleted(anyString())).thenReturn(Optional.empty());

        IllegalStateException exception =
                Assertions.assertThrows(IllegalStateException.class, () -> productSeeder.seed());

        Assertions.assertEquals(
                "Product 'NVME-1' refers to unknown product type 'Shared Hosting'",
                exception.getMessage());
        verify(productRepo, never()).saveAll(anyList());
    }
}
