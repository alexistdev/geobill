package com.alexistdev.geobill.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.util.Set;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HostingRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test HostingRequest Getters and Setters")
    void testHostingRequestGettersAndSetters() {
        HostingRequest hostingRequest = new HostingRequest();
        String userId = String.valueOf(UUID.randomUUID());
        String productId = String.valueOf(UUID.randomUUID());
        String domainName = "example.com";
        Double price = 100.0;
        Integer cycle = 1;

        hostingRequest.setUserId(userId);
        hostingRequest.setProductId(productId);
        hostingRequest.setDomainName(domainName);
        hostingRequest.setPrice(price);
        hostingRequest.setCycle(cycle);

        Assertions.assertNotNull(hostingRequest.getPrice());

        Assertions.assertEquals(userId, hostingRequest.getUserId());
        Assertions.assertEquals(productId, hostingRequest.getProductId());
        Assertions.assertEquals(domainName, hostingRequest.getDomainName());
        Assertions.assertEquals(price, hostingRequest.getPrice());
        Assertions.assertEquals(cycle, hostingRequest.getCycle());
        Assertions.assertEquals(Double.class, hostingRequest.getPrice().getClass());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test HostingRequest Default Constructor")
    void testHostingRequestDefaultConstructor() {
        HostingRequest hostingRequest = new HostingRequest();
        Assertions.assertNull(hostingRequest.getUserId());
        Assertions.assertNull(hostingRequest.getProductId());
        Assertions.assertNull(hostingRequest.getPrice());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test HostingRequest Validation - Invalid userId")
    void testHostingRequestValidationInvalidUserId() {
        HostingRequest hostingRequest = new HostingRequest();
        hostingRequest.setUserId("invalid-uuid");
        hostingRequest.setProductId(String.valueOf(UUID.randomUUID()));
        hostingRequest.setDomainName("example.com");
        hostingRequest.setPrice(100.0);
        hostingRequest.setCycle(1);

        Set<ConstraintViolation<HostingRequest>> violations = validator.validate(hostingRequest);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertEquals(1, violations.size()); // Assuming only userId is invalid
        Assertions.assertEquals("userId must be a valid UUID", violations.iterator().next().getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test HostingRequest Validation - Invalid productId")
    void testHostingRequestValidationInvalidProductId() {
        HostingRequest hostingRequest = new HostingRequest();
        hostingRequest.setUserId(String.valueOf(UUID.randomUUID()));
        hostingRequest.setProductId("invalid-uuid");
        hostingRequest.setDomainName("example.com");
        hostingRequest.setPrice(100.0);
        hostingRequest.setCycle(1);

        Set<ConstraintViolation<HostingRequest>> violations = validator.validate(hostingRequest);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertEquals(1, violations.size()); // Assuming only productId is invalid
        Assertions.assertEquals("productId must be a valid UUID", violations.iterator().next().getMessage());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test HostingRequest Validation - Blank domainName")
    void testHostingRequestValidationBlankDomainName() {
        HostingRequest hostingRequest = new HostingRequest();
        hostingRequest.setUserId(String.valueOf(UUID.randomUUID()));
        hostingRequest.setProductId(String.valueOf(UUID.randomUUID()));
        hostingRequest.setDomainName("");
        hostingRequest.setPrice(100.0);
        hostingRequest.setCycle(1);

        Set<ConstraintViolation<HostingRequest>> violations = validator.validate(hostingRequest);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("domainName is required", violations.iterator().next().getMessage());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test HostingRequest Validation - Price less than 0")
    void testHostingRequestValidationPriceLessThanZero() {
        HostingRequest hostingRequest = new HostingRequest();
        hostingRequest.setUserId(String.valueOf(UUID.randomUUID()));
        hostingRequest.setProductId(String.valueOf(UUID.randomUUID()));
        hostingRequest.setDomainName("example.com");
        hostingRequest.setPrice(0.0);
        hostingRequest.setCycle(1);

        Set<ConstraintViolation<HostingRequest>> violations = validator.validate(hostingRequest);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("price must be greater than 0", violations.iterator().next().getMessage());
    }

    @Test
    @Order(7)
    @DisplayName("7. Test HostingRequest Validation - Cycle less than 1")
    void testHostingRequestValidationCycleLessThanOne() {
        HostingRequest hostingRequest = new HostingRequest();
        hostingRequest.setUserId(String.valueOf(UUID.randomUUID()));
        hostingRequest.setProductId(String.valueOf(UUID.randomUUID()));
        hostingRequest.setDomainName("example.com");
        hostingRequest.setPrice(100.0);
        hostingRequest.setCycle(0);

        Set<ConstraintViolation<HostingRequest>> violations = validator.validate(hostingRequest);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("cycle must be at least 1 month", violations.iterator().next().getMessage());
    }
}
