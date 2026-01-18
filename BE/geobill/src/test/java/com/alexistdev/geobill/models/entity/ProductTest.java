package com.alexistdev.geobill.models.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

public class ProductTest {
    private Product product;
    private ProductType productType;
    private UUID id;
    private String name;
    private Double price;
    private Integer cycle;
    private String capacity;
    private String bandwith;
    private String addon_domain;
    private String database_account;
    private String ftp_account;
    private String info1;
    private String info2;
    private String info3;
    private String info4;
    private String info5;
    private static Validator validator;


    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {

        id = java.util.UUID.randomUUID();
        name = "Shared Hosting";
        price  = 10.0;
        cycle = 1;
        capacity = "10GB";
        bandwith = "1000 Mbps";
        addon_domain = "1";
        database_account = "1";
        ftp_account = "1";
        info1 = "info1";
        info2 = "info2";
        info3 = "info3";
        info4 = "info4";
        info5 = "info5";

        productType = new ProductType();
        productType.setId(id);
        productType.setName("Shared Hosting");

        product = new Product();
        product.setId(id);
        product.setName(name);
        product.setProductType(productType);
        product.setPrice(price);
        product.setCycle(cycle);
        product.setCapacity(capacity);
        product.setBandwith(bandwith);
        product.setAddon_domain(addon_domain);
        product.setDatabase_account(database_account);
        product.setFtp_account(ftp_account);
        product.setInfo1(info1);
        product.setInfo2(info2);
        product.setInfo3(info3);
        product.setInfo4(info4);
        product.setInfo5(info5);
        product.setCreatedBy("system");
        product.setCreatedDate(new java.util.Date());
        product.setModifiedBy("system");
        product.setModifiedDate(new java.util.Date());
        product.setIsDeleted(false);
    }

    @Test
    @DisplayName("Test Get data")
    void testGetData() {
        Assertions.assertNotNull(product.getId());
        Assertions.assertEquals(id, product.getId());
        Assertions.assertEquals(name, product.getName());
        Assertions.assertEquals(productType, product.getProductType());
        Assertions.assertEquals(price, product.getPrice());
        Assertions.assertEquals(cycle, product.getCycle());
        Assertions.assertEquals(capacity, product.getCapacity());
        Assertions.assertEquals(bandwith, product.getBandwith());
        Assertions.assertEquals(addon_domain, product.getAddon_domain());
        Assertions.assertEquals(database_account, product.getDatabase_account());
        Assertions.assertEquals(ftp_account, product.getFtp_account());
        Assertions.assertEquals(info1, product.getInfo1());
        Assertions.assertEquals(info2, product.getInfo2());
        Assertions.assertEquals(info3, product.getInfo3());
        Assertions.assertEquals(info4, product.getInfo4());
        Assertions.assertEquals(info5, product.getInfo5());

        Assertions.assertNotNull(product.getCreatedDate());
        Assertions.assertNotNull(product.getCreatedDate());
        Assertions.assertNotNull(product.getCreatedBy());
        Assertions.assertNotNull(product.getModifiedBy());
        Assertions.assertFalse(product.getIsDeleted());
    }

    @Test
    @DisplayName("Test Set data")
    void testSetData() {
        UUID newId = java.util.UUID.randomUUID();
        String newName = "VPS";
        Double newPrice  = 20.0;
        Integer newCycle = 2;
        String newCapacity = "100GB";
        String newBandwith = "Unlimited";
        String newAddon_domain = "Unlimited";
        String newDatabase_account = "Unlimited";
        String newFtp_account = "Unlimited";
        String newInfo1 = "newInfo1";
        String newInfo2 = "newInfo2";
        String newInfo3 = "newInfo3";
        String newInfo4 = "newInfo4";
        String newInfo5 = "newInfo5";

        product.setId(newId);
        product.setName(newName);
        product.setPrice(newPrice);
        product.setCycle(newCycle);
        product.setCapacity(newCapacity);
        product.setBandwith(newBandwith);
        product.setAddon_domain(newAddon_domain);
        product.setDatabase_account(newDatabase_account);
        product.setFtp_account(newFtp_account);
        product.setInfo1(newInfo1);
        product.setInfo2(newInfo2);
        product.setInfo3(newInfo3);
        product.setInfo4(newInfo4);
        product.setInfo5(newInfo5);

        Assertions.assertEquals(newId, product.getId());
        Assertions.assertEquals(newName, product.getName());
        Assertions.assertEquals(newPrice, product.getPrice());
        Assertions.assertEquals(newCycle, product.getCycle());
        Assertions.assertEquals(newCapacity, product.getCapacity());
        Assertions.assertEquals(newBandwith, product.getBandwith());
        Assertions.assertEquals(newAddon_domain, product.getAddon_domain());
        Assertions.assertEquals(newDatabase_account, product.getDatabase_account());
        Assertions.assertEquals(newFtp_account, product.getFtp_account());
        Assertions.assertEquals(newInfo1, product.getInfo1());
        Assertions.assertEquals(newInfo2, product.getInfo2());
    }

    @Test
    @DisplayName("Ensure Product is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class,product);
    }

    @Test
    @DisplayName("Should verify Menu extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class,product);
    }

    @Test
    @DisplayName("Should verify id is UUID")
    void testIdTypeIsUUID() {
        try{
            Field field = Product.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException  e){
            fail("id field should exist in ProductType class");
        }
    }

    @Test
    @DisplayName("Should not allow null value")
    public void testNullNotAllowed() {
        product.setName(null);
        product.setProductType(null);
        product.setPrice(null);
        product.setCycle(null);
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        Assertions.assertFalse(violations.isEmpty(),
                "Should have validation errors when name is null");

        Assertions.assertEquals(4, violations.size(),
                "Should have exactly 4 validation errors for null mandatory fields");

        List<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        Assertions.assertTrue(violatedProperties.contains("name"), "Missing violation for 'name'");
        Assertions.assertTrue(violatedProperties.contains("productType"), "Missing violation for 'productType'");
        Assertions.assertTrue(violatedProperties.contains("price"), "Missing violation for 'price'");
        Assertions.assertTrue(violatedProperties.contains("cycle"), "Missing violation for 'cycle'");

    }

    @Test
    @DisplayName("Should not allow blank name")
    void testBlankNameNotAllowed() {
        product.setName("");
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        boolean hasNameViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));

        Assertions.assertTrue(hasNameViolation, "Should have a validation error specifically for the 'name' field");
    }

    @Test
    @DisplayName("Test Equals and HashCode")
    void testEqualsAndHashCode() {
        Product anotherProduct = new Product();
        anotherProduct.setId(product.getId());

        Assertions.assertEquals(product, anotherProduct, "Products with same ID should be equal");
        Assertions.assertEquals(product.hashCode(), anotherProduct.hashCode(), "HashCodes should match for same ID");

        anotherProduct.setId(UUID.randomUUID());
        Assertions.assertNotEquals(product, anotherProduct, "Products with different IDs should not be equal");
    }
}
