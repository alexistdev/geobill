package com.alexistdev.geobill.seeder;

import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.entity.ProductType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Date;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeedAuditTest {

    private ProductType productType;

    @BeforeEach
    void setUp() {
        productType = new ProductType();
        productType.setName("Shared Hosting");
    }

    @Test
    @Order(1)
    @DisplayName("1:Test Stamp Fills Audit Columns")
    void testStampFillsAuditColumns() {
        ProductType stamped = SeedAudit.stamp(productType);

        Assertions.assertEquals(SeedAudit.SYSTEM_USER, stamped.getCreatedBy());
        Assertions.assertEquals(SeedAudit.SYSTEM_USER, stamped.getModifiedBy());
        Assertions.assertNotNull(stamped.getCreatedDate());
        Assertions.assertNotNull(stamped.getModifiedDate());
        Assertions.assertFalse(stamped.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Stamp Returns The Same Instance")
    void testStampReturnsSameInstance() {
        ProductType stamped = SeedAudit.stamp(productType);

        Assertions.assertSame(productType, stamped);
        Assertions.assertEquals("Shared Hosting", stamped.getName());
    }

    @Test
    @Order(3)
    @DisplayName("3:Test Stamp Uses The Same Date For Created And Modified")
    void testStampUsesSameDate() {
        ProductType stamped = SeedAudit.stamp(productType);

        Assertions.assertEquals(stamped.getCreatedDate(), stamped.getModifiedDate());
    }

    @Test
    @Order(4)
    @DisplayName("4:Test Stamp Overwrites Existing Audit Values")
    void testStampOverwritesExistingValues() {
        Date past = new Date(0L);
        Menu menu = new Menu();
        menu.setCode("ADM_DASHBOARD");
        menu.setCreatedBy("someone");
        menu.setModifiedBy("someone");
        menu.setCreatedDate(past);
        menu.setModifiedDate(past);
        menu.setDeleted(Boolean.TRUE);

        Menu stamped = SeedAudit.stamp(menu);

        Assertions.assertEquals(SeedAudit.SYSTEM_USER, stamped.getCreatedBy());
        Assertions.assertEquals(SeedAudit.SYSTEM_USER, stamped.getModifiedBy());
        Assertions.assertNotEquals(past, stamped.getCreatedDate());
        Assertions.assertNotEquals(past, stamped.getModifiedDate());
        Assertions.assertFalse(stamped.getDeleted());
    }

    @Test
    @Order(5)
    @DisplayName("5:Test System User Constant")
    void testSystemUserConstant() {
        Assertions.assertEquals("System", SeedAudit.SYSTEM_USER);
    }

    @Test
    @Order(6)
    @DisplayName("6:Test Utility Class Cannot Be Instantiated")
    void testUtilityClassIsNotInstantiable() throws Exception {
        Constructor<SeedAudit> constructor = SeedAudit.class.getDeclaredConstructor();

        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Assertions.assertNotNull(constructor.newInstance());
    }
}
