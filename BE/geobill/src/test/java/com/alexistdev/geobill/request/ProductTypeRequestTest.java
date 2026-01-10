package com.alexistdev.geobill.request;

import com.alexistdev.geobill.config.ValidationConstant;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

public class ProductTypeRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Test Getter And Setter")
    void testProductTypeRequestGettersAndSetters() {

        ProductTypeRequest productTypeRequest = new ProductTypeRequest();
        String testName = "Shared Hosting";
        UUID testId = UUID.randomUUID();

        productTypeRequest.setName(testName);
        productTypeRequest.setId(testId);

        Assertions.assertEquals(testName, productTypeRequest.getName());
        Assertions.assertEquals(testId, productTypeRequest.getId());
    }

    @Test
    @DisplayName("Test Default Constructor")
    void testProductTypeRequestDefaultConstructor() {
        ProductTypeRequest productTypeRequest = new ProductTypeRequest();
        Assertions.assertNull(productTypeRequest.getName());
        Assertions.assertNull(productTypeRequest.getId());
    }

    @Test
    @DisplayName("Test Name Not Empty Validation")
    void testNameNotEmptyValidation() {
        ProductTypeRequest productTypeRequest = new ProductTypeRequest();
        productTypeRequest.setName(null);

        Set<ConstraintViolation<ProductTypeRequest>> violations =
                validator.validate(productTypeRequest);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("name")));

    }

    @Test
    @DisplayName("Test Name Size Validation")
    void testNameSizeValidation() {
        ProductTypeRequest productTypeRequest = new ProductTypeRequest();
        String maxName = "a".repeat(151);
        productTypeRequest.setName(maxName);

        Set<ConstraintViolation<ProductTypeRequest>> violations = validator.validate(productTypeRequest);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("name")));
        Assertions.assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getMessage().equals(ValidationConstant.nameMax)),
                "Violation message should match ValidationConstant.nameMax");
    }

    @Test
    @DisplayName("Test Name Size Validation at Maximum")
    void testNameSizeValidation_atMaximum() {
        ProductTypeRequest productTypeRequest = new ProductTypeRequest();
        String maxName = "a".repeat(150);
        productTypeRequest.setName(maxName);

        Set<ConstraintViolation<ProductTypeRequest>> violations =
                validator.validate(productTypeRequest);
        Assertions.assertTrue(violations.isEmpty());
    }
}
