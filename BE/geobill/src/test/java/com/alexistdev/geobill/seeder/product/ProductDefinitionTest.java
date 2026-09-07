package com.alexistdev.geobill.seeder.product;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductDefinitionTest {

    @Test
    @Order(1)
    @DisplayName("1:Test Builder Fills Every Field")
    void testBuilderFillsEveryField() {
        ProductDefinition definition = ProductDefinition.builder()
                .productTypeName("Shared Hosting")
                .name("NVME-1")
                .price(50_000.0)
                .cycle(12)
                .capacity("10GB")
                .bandwith("1000 Mbps")
                .addonDomain("1")
                .databaseAccount("1")
                .ftpAccount("1")
                .info1("1 GB RAM")
                .info2("2 Core")
                .info3("PHP")
                .info4("info4")
                .info5("info5")
                .build();

        Assertions.assertEquals("Shared Hosting", definition.productTypeName());
        Assertions.assertEquals("NVME-1", definition.name());
        Assertions.assertEquals(50_000.0, definition.price());
        Assertions.assertEquals(12, definition.cycle());
        Assertions.assertEquals("10GB", definition.capacity());
        Assertions.assertEquals("1000 Mbps", definition.bandwith());
        Assertions.assertEquals("1", definition.addonDomain());
        Assertions.assertEquals("1", definition.databaseAccount());
        Assertions.assertEquals("1", definition.ftpAccount());
        Assertions.assertEquals("1 GB RAM", definition.info1());
        Assertions.assertEquals("2 Core", definition.info2());
        Assertions.assertEquals("PHP", definition.info3());
        Assertions.assertEquals("info4", definition.info4());
        Assertions.assertEquals("info5", definition.info5());
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Fields Left Out Of The Builder Keep Their Default")
    void testUnsetFieldsKeepDefaults() {
        ProductDefinition definition = ProductDefinition.builder()
                .productTypeName("VPS")
                .name("VPS-1")
                .build();

        Assertions.assertEquals("VPS", definition.productTypeName());
        Assertions.assertEquals(0.0, definition.price());
        Assertions.assertEquals(0, definition.cycle());
        Assertions.assertNull(definition.capacity());
        Assertions.assertNull(definition.info4());
        Assertions.assertNull(definition.info5());
    }

    @Test
    @Order(3)
    @DisplayName("3:Test Definitions With The Same Values Are Equal")
    void testEquality() {
        ProductDefinition first = ProductDefinition.builder().name("NVME-1").price(50_000.0).build();
        ProductDefinition second = ProductDefinition.builder().name("NVME-1").price(50_000.0).build();
        ProductDefinition other = ProductDefinition.builder().name("NVME-2").price(50_000.0).build();

        Assertions.assertEquals(first, second);
        Assertions.assertEquals(first.hashCode(), second.hashCode());
        Assertions.assertNotEquals(first, other);
    }
}
