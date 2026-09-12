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
public class TicketDepartmentTest {

    private UUID id;
    private TicketDepartment department;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();

        department = new TicketDepartment();
        department.setId(id);
        department.setName("Technical Support");
        department.setCode("TECH");
        department.setEmail("support@geobill.test");
        department.setDescription("Kendala teknis layanan hosting");
        department.setSortOrder(1);
        department.setIsActive(Boolean.TRUE);
        department.setAutoCloseDays(7);
        department.setDeleted(false);
        department.setCreatedBy("system");
        department.setCreatedDate(new Date());
        department.setModifiedBy("system");
        department.setModifiedDate(new Date());
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get data")
    void testGetData() {
        Assertions.assertEquals(id, department.getId());
        Assertions.assertEquals("Technical Support", department.getName());
        Assertions.assertEquals("TECH", department.getCode());
        Assertions.assertEquals("support@geobill.test", department.getEmail());
        Assertions.assertEquals("Kendala teknis layanan hosting", department.getDescription());
        Assertions.assertEquals(1, department.getSortOrder());
        Assertions.assertTrue(department.getIsActive());
        Assertions.assertEquals(7, department.getAutoCloseDays());
        Assertions.assertNotNull(department.getCreatedDate());
        Assertions.assertNotNull(department.getModifiedDate());
        Assertions.assertNotNull(department.getCreatedBy());
        Assertions.assertNotNull(department.getModifiedBy());
        Assertions.assertFalse(department.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();

        department.setId(newId);
        department.setName("Billing");
        department.setCode("BILLING");
        department.setEmail("billing@geobill.test");
        department.setDescription("Pertanyaan tagihan");
        department.setSortOrder(2);
        department.setIsActive(Boolean.FALSE);
        department.setAutoCloseDays(null);

        Assertions.assertEquals(newId, department.getId());
        Assertions.assertEquals("Billing", department.getName());
        Assertions.assertEquals("BILLING", department.getCode());
        Assertions.assertEquals("billing@geobill.test", department.getEmail());
        Assertions.assertEquals("Pertanyaan tagihan", department.getDescription());
        Assertions.assertEquals(2, department.getSortOrder());
        Assertions.assertFalse(department.getIsActive());
        Assertions.assertNull(department.getAutoCloseDays());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure TicketDepartment is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, department);
    }

    @Test
    @Order(4)
    @DisplayName("4. Should verify TicketDepartment extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, department);
    }

    @Test
    @Order(5)
    @DisplayName("5. Should verify id is UUID")
    void testIdTypeIsUUID() {
        try {
            Field field = TicketDepartment.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException e) {
            Assertions.fail("Field 'id' not found in TicketDepartment class");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Should not allow null value")
    void testNullNotAllowed() {
        department.setName(null);
        department.setCode(null);
        department.setSortOrder(null);
        department.setIsActive(null);

        Set<ConstraintViolation<TicketDepartment>> violations = validator.validate(department);
        Assertions.assertEquals(4, violations.size(),
                "Should have exactly 4 validation errors for null mandatory fields");

        List<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        Assertions.assertTrue(violatedProperties.contains("name"), "Missing violation for 'name'");
        Assertions.assertTrue(violatedProperties.contains("code"), "Missing violation for 'code'");
        Assertions.assertTrue(violatedProperties.contains("sortOrder"), "Missing violation for 'sortOrder'");
        Assertions.assertTrue(violatedProperties.contains("isActive"), "Missing violation for 'isActive'");
    }

    @Test
    @Order(7)
    @DisplayName("7. Email, deskripsi, dan auto close boleh kosong")
    void testOptionalFieldsMayBeNull() {
        department.setEmail(null);
        department.setDescription(null);
        department.setAutoCloseDays(null);

        Set<ConstraintViolation<TicketDepartment>> violations = validator.validate(department);
        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    @Order(8)
    @DisplayName("8. Nilai default departemen baru")
    void testDefaultValues() {
        TicketDepartment fresh = new TicketDepartment();

        Assertions.assertTrue(fresh.getIsActive(), "Departemen baru langsung aktif");
        Assertions.assertEquals(0, fresh.getSortOrder());
        Assertions.assertFalse(fresh.getDeleted());
        Assertions.assertNull(fresh.getAutoCloseDays(), "Null berarti tidak pernah menutup tiket otomatis");
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Equals and HashCode")
    void testEqualsAndHashCode() {
        TicketDepartment sameId = new TicketDepartment();
        sameId.setId(department.getId());
        sameId.setName("Nama lain");

        TicketDepartment otherId = new TicketDepartment();
        otherId.setId(UUID.randomUUID());

        Assertions.assertEquals(department, sameId);
        Assertions.assertEquals(department.hashCode(), sameId.hashCode());
        Assertions.assertNotEquals(department, otherId);
    }
}
