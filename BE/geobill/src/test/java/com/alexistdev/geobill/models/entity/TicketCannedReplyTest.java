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
public class TicketCannedReplyTest {

    private UUID id;
    private UUID departmentId;
    private TicketCannedReply cannedReply;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        departmentId = UUID.randomUUID();

        TicketDepartment department = new TicketDepartment();
        department.setId(departmentId);

        cannedReply = new TicketCannedReply();
        cannedReply.setId(id);
        cannedReply.setDepartment(department);
        cannedReply.setTitle("Minta akses cPanel");
        cannedReply.setBody("Mohon kirimkan akses cPanel sementara agar kami bisa memeriksa.");
        cannedReply.setIsActive(Boolean.TRUE);
        cannedReply.setSortOrder(1);
        cannedReply.setDeleted(false);
        cannedReply.setCreatedBy("system");
        cannedReply.setCreatedDate(new Date());
        cannedReply.setModifiedBy("system");
        cannedReply.setModifiedDate(new Date());
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get data")
    void testGetData() {
        Assertions.assertEquals(id, cannedReply.getId());
        Assertions.assertEquals(departmentId, cannedReply.getDepartment().getId());
        Assertions.assertEquals("Minta akses cPanel", cannedReply.getTitle());
        Assertions.assertEquals("Mohon kirimkan akses cPanel sementara agar kami bisa memeriksa.",
                cannedReply.getBody());
        Assertions.assertTrue(cannedReply.getIsActive());
        Assertions.assertEquals(1, cannedReply.getSortOrder());
        Assertions.assertNotNull(cannedReply.getCreatedDate());
        Assertions.assertNotNull(cannedReply.getModifiedDate());
        Assertions.assertNotNull(cannedReply.getCreatedBy());
        Assertions.assertNotNull(cannedReply.getModifiedBy());
        Assertions.assertFalse(cannedReply.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();

        cannedReply.setId(newId);
        cannedReply.setTitle("Konfirmasi pembayaran");
        cannedReply.setBody("Pembayaran Anda sudah kami terima.");
        cannedReply.setIsActive(Boolean.FALSE);
        cannedReply.setSortOrder(3);

        Assertions.assertEquals(newId, cannedReply.getId());
        Assertions.assertEquals("Konfirmasi pembayaran", cannedReply.getTitle());
        Assertions.assertEquals("Pembayaran Anda sudah kami terima.", cannedReply.getBody());
        Assertions.assertFalse(cannedReply.getIsActive());
        Assertions.assertEquals(3, cannedReply.getSortOrder());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure TicketCannedReply is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, cannedReply);
    }

    @Test
    @Order(4)
    @DisplayName("4. Should verify TicketCannedReply extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, cannedReply);
    }

    @Test
    @Order(5)
    @DisplayName("5. Should verify id is UUID")
    void testIdTypeIsUUID() {
        try {
            Field field = TicketCannedReply.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException e) {
            Assertions.fail("Field 'id' not found in TicketCannedReply class");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Should not allow null value")
    void testNullNotAllowed() {
        cannedReply.setTitle(null);
        cannedReply.setBody(null);
        cannedReply.setIsActive(null);
        cannedReply.setSortOrder(null);

        Set<ConstraintViolation<TicketCannedReply>> violations = validator.validate(cannedReply);
        Assertions.assertEquals(4, violations.size(),
                "Should have exactly 4 validation errors for null mandatory fields");

        List<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        Assertions.assertTrue(violatedProperties.contains("title"), "Missing violation for 'title'");
        Assertions.assertTrue(violatedProperties.contains("body"), "Missing violation for 'body'");
        Assertions.assertTrue(violatedProperties.contains("isActive"), "Missing violation for 'isActive'");
        Assertions.assertTrue(violatedProperties.contains("sortOrder"), "Missing violation for 'sortOrder'");
    }

    @Test
    @Order(7)
    @DisplayName("7. Template tanpa departemen berlaku global")
    void testDepartmentMayBeNull() {
        cannedReply.setDepartment(null);

        Set<ConstraintViolation<TicketCannedReply>> violations = validator.validate(cannedReply);
        Assertions.assertTrue(violations.isEmpty());
        Assertions.assertNull(cannedReply.getDepartment());
    }

    @Test
    @Order(8)
    @DisplayName("8. Nilai default template baru")
    void testDefaultValues() {
        TicketCannedReply fresh = new TicketCannedReply();

        Assertions.assertTrue(fresh.getIsActive());
        Assertions.assertEquals(0, fresh.getSortOrder());
        Assertions.assertFalse(fresh.getDeleted());
        Assertions.assertNull(fresh.getDepartment());
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Equals and HashCode")
    void testEqualsAndHashCode() {
        TicketCannedReply sameId = new TicketCannedReply();
        sameId.setId(cannedReply.getId());

        TicketCannedReply otherId = new TicketCannedReply();
        otherId.setId(UUID.randomUUID());

        Assertions.assertEquals(cannedReply, sameId);
        Assertions.assertEquals(cannedReply.hashCode(), sameId.hashCode());
        Assertions.assertNotEquals(cannedReply, otherId);
    }
}
