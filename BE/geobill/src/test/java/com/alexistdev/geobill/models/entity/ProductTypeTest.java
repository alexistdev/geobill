package com.alexistdev.geobill.models.entity;

import org.junit.jupiter.api.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

public class ProductTypeTest {

    private ProductType productType;
    private UUID id;
    private static Validator validator;


    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        productType = new ProductType();
        id = UUID.randomUUID();
        productType.setId(id);
        productType.setName("Shared Hosting");
        productType.setCreatedBy("system");
        productType.setCreatedDate(new java.util.Date());
        productType.setModifiedBy("system");
        productType.setModifiedDate(new java.util.Date());
        productType.setIsDeleted(false);
    }

    @Test
    @DisplayName("Should return id of saved productType")
    void testSendAndGetId() {
        Assertions.assertEquals(id, productType.getId());
    }

    @Test
    @DisplayName("Should return name of saved productType")
    void testSendAndGetName() {
        Assertions.assertEquals("Shared Hosting", productType.getName());
    }

    @Test
    @DisplayName("Should allow to set new valid value")
    void testSetNewValidValue() {
        String newName = "VPS";
        productType.setName(newName);
        Assertions.assertEquals(newName, productType.getName());
    }

    @Test
    @DisplayName("Ensure ProductType is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(Serializable.class,productType);
    }

    @Test
    @DisplayName("Should verify Menu extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class,productType);
    }

    @Test
    @DisplayName("Should verify id is UUID")
    void testIdTypeIsUUID() {
        try{
            Field field = ProductType.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException  e){
            fail("id field should exist in ProductType class");
        }
    }

    @Test
    @DisplayName("Should not allow null value for name")
    void testNullName() {
        productType.setName(null);
        Set<ConstraintViolation<ProductType>> violations = validator.validate(productType);

        Assertions.assertFalse(violations.isEmpty(),
                "Should have validation errors when name is null");

        boolean hasNullNameViolation = violations.stream()
                .anyMatch(
                        violation -> violation.getPropertyPath()
                                .toString().equals("name"));

        Assertions.assertTrue(hasNullNameViolation,
                "Should have a validation error on the 'name' field");
    }
}
