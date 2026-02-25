package com.alexistdev.geobill.models.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HostingTest {
    private UUID id;
    private UUID userId;
    private UUID productId;
    private Hosting hosting;
    private Long hostingCode;
    private String name;
    private String domain;
    private Double price;
    private Date startDate;
    private Date endDate;
    private int status;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        hostingCode = 123456789L;
        name = "Hosting 1";
        domain = "hosting1.com";
        price = 100.0;
        startDate = new Date();
        endDate = new Date();
        status = 0;

        User user = new User();
        user.setId(userId);

        Product product = new Product();
        product.setId(productId);

        hosting = new Hosting();
        hosting.setId(id);
        hosting.setUser(user);
        hosting.setProduct(product);
        hosting.setHostingCode(hostingCode);
        hosting.setName(name);
        hosting.setDomain(domain);
        hosting.setPrice(price);
        hosting.setStartDate(startDate);
        hosting.setEndDate(endDate);
        hosting.setStatus(status);
        hosting.setDeleted(false);
        hosting.setCreatedBy("system");
        hosting.setCreatedDate(new Date());
        hosting.setModifiedBy("system");
        hosting.setModifiedDate(new Date());
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get Data")
    void testGetData() {
        Assertions.assertNotNull(hosting.getId());
        Assertions.assertEquals(id, hosting.getId());
        Assertions.assertEquals(userId, hosting.getUser().getId());
        Assertions.assertEquals(productId, hosting.getProduct().getId());
        Assertions.assertEquals(hostingCode, hosting.getHostingCode());
        Assertions.assertEquals(name, hosting.getName());
        Assertions.assertEquals(domain, hosting.getDomain());
        Assertions.assertEquals(price, hosting.getPrice());
        Assertions.assertEquals(startDate, hosting.getStartDate());
        Assertions.assertEquals(endDate, hosting.getEndDate());
        Assertions.assertEquals(status, hosting.getStatus());
        Assertions.assertNotNull(hosting.getCreatedDate());
        Assertions.assertNotNull(hosting.getModifiedDate());
        Assertions.assertNotNull(hosting.getCreatedBy());
        Assertions.assertNotNull(hosting.getModifiedBy());
        Assertions.assertFalse(hosting.getDeleted());
        Assertions.assertNotNull(hosting.getProduct());
        Assertions.assertNotNull(hosting.getUser());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set Data")
    void testSetData(){
        UUID newId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        UUID newProductId = UUID.randomUUID();
        User newUser = new User();
        newUser.setId(newUserId);
        Product newProduct = new Product();
        newProduct.setId(newProductId);

        Long newHostingCode = 123L;
        String newName = "Hosting New";
        String newDomain = "hostingNew.com";
        Double newPrice = 200.0;
        Date newStartDate = new Date();
        Date newEndDate = new Date();
        int newStatus = 0;

        hosting.setId(newId);
        hosting.setUser(newUser);
        hosting.setProduct(newProduct);
        hosting.setHostingCode(newHostingCode);
        hosting.setName(newName);
        hosting.setDomain(newDomain);
        hosting.setPrice(newPrice);
        hosting.setStartDate(newStartDate);
        hosting.setEndDate(newEndDate);
        hosting.setStatus(newStatus);

        Assertions.assertEquals(newId, hosting.getId());
        Assertions.assertEquals(newUserId, hosting.getUser().getId());
        Assertions.assertEquals(newProductId, hosting.getProduct().getId());
        Assertions.assertEquals(newHostingCode, hosting.getHostingCode());
        Assertions.assertEquals(newName, hosting.getName());
        Assertions.assertEquals(newDomain, hosting.getDomain());
        Assertions.assertEquals(newPrice, hosting.getPrice());
        Assertions.assertEquals(newStartDate, hosting.getStartDate());
        Assertions.assertEquals(newEndDate, hosting.getEndDate());
        Assertions.assertEquals(newStatus, hosting.getStatus());

    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure Product is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class,hosting);
    }

    @Test
    @Order(4)
    @DisplayName("4. Should verify Menu extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class,hosting);
    }

    @Test
    @Order(5)
    @DisplayName("5. Should verify id is UUID")
    void testIdIsUUID() {
        try{
            Field field = Hosting.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException  e){
            fail("id field should exist in ProductType class");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Should not allow null value")
    public void testNullNotAllowed() {
        hosting.setUser(null);
        hosting.setProduct(null);
        hosting.setName(null);
        hosting.setDomain(null);
        hosting.setPrice(null);
        hosting.setStartDate(null);
        hosting.setEndDate(null);
        Set<ConstraintViolation<Hosting>> violations = validator.validate(hosting);
        Assertions.assertFalse(violations.isEmpty(),
                "Should have validation errors when fields are null");

        Assertions.assertEquals(7, violations.size(),
                "Should have exactly 7 validation errors for null mandatory fields");

        List<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        Assertions.assertTrue(violatedProperties.contains("user"), "Missing violation for 'user'");
        Assertions.assertTrue(violatedProperties.contains("product"), "Missing violation for 'product'");
        Assertions.assertTrue(violatedProperties.contains("name"), "Missing violation for 'name'");
        Assertions.assertTrue(violatedProperties.contains("domain"), "Missing violation for 'domain'");
        Assertions.assertTrue(violatedProperties.contains("price"), "Missing violation for 'price'");
        Assertions.assertTrue(violatedProperties.contains("startDate"), "Missing violation for 'startDate'");
        Assertions.assertTrue(violatedProperties.contains("endDate"), "Missing violation for 'endDate'");
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Equals and HashCode")
    void testEqualsAndHashCode() {
        Hosting anotherHosting = new Hosting();
        anotherHosting.setId(hosting.getId());
        anotherHosting.setUser(hosting.getUser());
        anotherHosting.setProduct(hosting.getProduct());

        Assertions.assertEquals(hosting, anotherHosting, "Hosting with same ID should be equal");
        Assertions.assertEquals(hosting.hashCode(), anotherHosting.hashCode(), "HashCodes should match for same ID");
    }
}
