package com.alexistdev.geobill.seeder.product;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductCatalogTest {

    @Test
    @Order(1)
    @DisplayName("1:Test Declared Product Types")
    void testProductTypes() {
        List<String> productTypes = ProductCatalog.productTypes();

        Assertions.assertEquals(2, productTypes.size());
        Assertions.assertEquals(List.of("Shared Hosting", "VPS"), productTypes);
        Assertions.assertEquals("Shared Hosting", ProductCatalog.SHARED_HOSTING);
        Assertions.assertEquals("VPS", ProductCatalog.VPS);
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Declared Products")
    void testProducts() {
        List<ProductDefinition> products = ProductCatalog.products();

        Assertions.assertEquals(3, products.size());
        Assertions.assertEquals("NVME-1", products.get(0).name());
        Assertions.assertEquals("NVME-2", products.get(1).name());
        Assertions.assertEquals("NVME-3", products.get(2).name());
    }

    @Test
    @Order(3)
    @DisplayName("3:Test Every Product Refers To A Declared Product Type")
    void testProductTypeNamesExist() {
        List<String> productTypes = ProductCatalog.productTypes();

        for (ProductDefinition definition : ProductCatalog.products()) {
            Assertions.assertTrue(productTypes.contains(definition.productTypeName()),
                    "Unknown product type: " + definition.productTypeName());
        }
    }

    @Test
    @Order(4)
    @DisplayName("4:Test Product Names Are Unique")
    void testProductNamesAreUnique() {
        List<ProductDefinition> products = ProductCatalog.products();

        long distinct = products.stream().map(ProductDefinition::name).distinct().count();

        Assertions.assertEquals(products.size(), distinct);
    }

    @Test
    @Order(5)
    @DisplayName("5:Test Values Of The First Product")
    void testFirstProductValues() {
        ProductDefinition definition = ProductCatalog.products().getFirst();

        Assertions.assertEquals(ProductCatalog.SHARED_HOSTING, definition.productTypeName());
        Assertions.assertEquals(50_000.0, definition.price());
        Assertions.assertEquals(12, definition.cycle());
        Assertions.assertEquals("10GB", definition.capacity());
        Assertions.assertEquals("1000 Mbps", definition.bandwith());
        Assertions.assertEquals("1", definition.addonDomain());
        Assertions.assertEquals("1", definition.databaseAccount());
        Assertions.assertEquals("1", definition.ftpAccount());
        Assertions.assertEquals("1 GB RAM", definition.info1());
        Assertions.assertEquals("2 Core", definition.info2());
        Assertions.assertEquals("PHP, Ruby, Python, NodeJS", definition.info3());
    }

    @Test
    @Order(6)
    @DisplayName("6:Test Every Product Has A Name, A Price And A Cycle")
    void testEveryProductIsComplete() {
        for (ProductDefinition definition : ProductCatalog.products()) {
            Assertions.assertNotNull(definition.name());
            Assertions.assertFalse(definition.name().isBlank());
            Assertions.assertTrue(definition.price() > 0);
            Assertions.assertTrue(definition.cycle() > 0);
        }
    }

    @Test
    @Order(7)
    @DisplayName("7:Test Catalog Lists Are Immutable")
    void testCatalogListsAreImmutable() {
        List<String> productTypes = ProductCatalog.productTypes();
        List<ProductDefinition> products = ProductCatalog.products();

        Assertions.assertThrows(UnsupportedOperationException.class, () -> productTypes.add("Cloud"));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> products.add(ProductDefinition.builder().name("NVME-4").build()));
    }

    @Test
    @Order(8)
    @DisplayName("8:Test Utility Class Cannot Be Instantiated")
    void testUtilityClassIsNotInstantiable() throws Exception {
        Constructor<ProductCatalog> constructor = ProductCatalog.class.getDeclaredConstructor();

        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Assertions.assertNotNull(constructor.newInstance());
    }
}
