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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvoiceTest {
    private UUID id;
    private UUID userId;
    private UUID hostingId;
    private Invoice invoice;
    private String invoiceCode;
    private String detail;
    private Double subTotal;
    private Double total;
    private Double tax;
    private Double discount;
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
        hostingId = UUID.randomUUID();
        invoiceCode = "INV-001";
        detail = "Invoice Detail";
        subTotal = 100.00;
        total = 120.00;
        tax = 20.00;
        discount = 10.00;
        startDate = new Date();
        endDate = new Date();
        status = 0;

        User  user = new User();
        user.setId(userId);

        Hosting hosting = new Hosting();
        hosting.setId(hostingId);

        invoice = new Invoice();
        invoice.setId(id);
        invoice.setUser(user);
        invoice.setHosting(hosting);
        invoice.setInvoiceCode(invoiceCode);
        invoice.setDetail(detail);
        invoice.setSubTotal(subTotal);
        invoice.setTotal(total);
        invoice.setTax(tax);
        invoice.setDiscount(discount);
        invoice.setStartDate(startDate);
        invoice.setEndDate(endDate);
        invoice.setStatus(status);
        invoice.setDeleted(false);
        invoice.setCreatedBy("system");
        invoice.setCreatedDate(new Date());
        invoice.setModifiedBy("system");
        invoice.setModifiedDate(new Date());
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get data")
    void testGetData() {
        Assertions.assertNotNull(invoice.getId());
        Assertions.assertEquals(id, invoice.getId());
        Assertions.assertEquals(userId, invoice.getUser().getId());
        Assertions.assertEquals(hostingId, invoice.getHosting().getId());
        Assertions.assertEquals(invoiceCode, invoice.getInvoiceCode());
        Assertions.assertEquals(detail, invoice.getDetail());
        Assertions.assertEquals(subTotal, invoice.getSubTotal());
        Assertions.assertEquals(total, invoice.getTotal());
        Assertions.assertEquals(tax, invoice.getTax());
        Assertions.assertEquals(discount, invoice.getDiscount());
        Assertions.assertEquals(startDate, invoice.getStartDate());
        Assertions.assertEquals(endDate, invoice.getEndDate());
        Assertions.assertEquals(status, invoice.getStatus());
        Assertions.assertNotNull(invoice.getCreatedDate());
        Assertions.assertNotNull(invoice.getModifiedDate());
        Assertions.assertNotNull(invoice.getCreatedBy());
        Assertions.assertNotNull(invoice.getModifiedBy());
        Assertions.assertFalse(invoice.getDeleted());
        Assertions.assertNotNull(invoice.getUser());
        Assertions.assertNotNull(invoice.getHosting());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        UUID newHostingId = UUID.randomUUID();
        User newUser = new User();
        newUser.setId(newUserId);
        Hosting newHosting = new Hosting();
        newHosting.setId(newHostingId);

        String newInvoiceCode = "INV-002";
        String newDetail = "Invoice Detail 2";
        Double newSubTotal = 200.00;
        Double newTotal = 130.00;
        Double newTax = 30.00;
        Double newDiscount = 20.00;
        Date newStartDate = new Date();
        Date newEndDate = new Date();
        int newStatus = 1;

        invoice.setId(newId);
        invoice.setUser(newUser);
        invoice.setHosting(newHosting);
        invoice.setInvoiceCode(newInvoiceCode);
        invoice.setDetail(newDetail);
        invoice.setSubTotal(newSubTotal);
        invoice.setTotal(newTotal);
        invoice.setTax(newTax);
        invoice.setDiscount(newDiscount);
        invoice.setStartDate(newStartDate);
        invoice.setEndDate(newEndDate);
        invoice.setStatus(newStatus);

        Assertions.assertEquals(newId, invoice.getId());
        Assertions.assertEquals(newUserId, invoice.getUser().getId());
        Assertions.assertEquals(newHostingId, invoice.getHosting().getId());
        Assertions.assertEquals(newInvoiceCode, invoice.getInvoiceCode());
        Assertions.assertEquals(newDetail, invoice.getDetail());
        Assertions.assertEquals(newSubTotal, invoice.getSubTotal());
        Assertions.assertEquals(newTotal, invoice.getTotal());
        Assertions.assertEquals(newTax, invoice.getTax());
        Assertions.assertEquals(newDiscount, invoice.getDiscount());
        Assertions.assertEquals(newStartDate, invoice.getStartDate());
        Assertions.assertEquals(newEndDate, invoice.getEndDate());
        Assertions.assertEquals(newStatus, invoice.getStatus());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure Invoice is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, invoice);
    }

    @Test
    @Order(4)
    @DisplayName("4. Should verify Menu extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, invoice);
    }

    @Test
    @Order(5)
    @DisplayName("5. Should verify id is UUID")
    void testIdTypeIsUUID() {
        try {
            Field field = Invoice.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException e) {
            Assertions.fail("Field 'id' not found in Invoice class");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Should not allow null value")
    void testNullNotAllowed() {
        invoice.setUser(null);
        invoice.setHosting(null);
        invoice.setInvoiceCode(null);
        invoice.setDetail(null);
        invoice.setSubTotal(null);
        invoice.setTotal(null);
        invoice.setTax(null);
        invoice.setDiscount(null);
        invoice.setStartDate(null);
        invoice.setEndDate(null);

        Set<ConstraintViolation<Invoice>> violations = validator.validate(invoice);
        Assertions.assertFalse(violations.isEmpty());

        Assertions.assertEquals(10, violations.size(),
                "Should have exactly 10 validation errors for null mandatory fields");

        List<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        Assertions.assertTrue(violatedProperties.contains("user"), "Missing violation for 'user'");
        Assertions.assertTrue(violatedProperties.contains("hosting"), "Missing violation for 'hosting'");
        Assertions.assertTrue(violatedProperties.contains("invoiceCode"), "Missing violation for 'invoiceCode'");
        Assertions.assertTrue(violatedProperties.contains("detail"), "Missing violation for 'detail'");
        Assertions.assertTrue(violatedProperties.contains("subTotal"), "Missing violation for 'subTotal'");
        Assertions.assertTrue(violatedProperties.contains("total"), "Missing violation for 'total'");
        Assertions.assertTrue(violatedProperties.contains("tax"), "Missing violation for 'tax'");
        Assertions.assertTrue(violatedProperties.contains("discount"), "Missing violation for 'discount'");
        Assertions.assertTrue(violatedProperties.contains("startDate"), "Missing violation for 'startDate'");
        Assertions.assertTrue(violatedProperties.contains("endDate"), "Missing violation for 'endDate'");
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Equals and HashCode")
    void testEqualsAndHashCode() {
        Invoice anotherInvoice = new Invoice();
        anotherInvoice.setId(invoice.getId());
        anotherInvoice.setUser(invoice.getUser());
        anotherInvoice.setHosting(invoice.getHosting());

        Assertions.assertEquals(invoice, anotherInvoice, "Invoice with same ID should be equal");
        Assertions.assertEquals(invoice.hashCode(), anotherInvoice.hashCode(), "HashCodes should match for same ID");
    }
}
