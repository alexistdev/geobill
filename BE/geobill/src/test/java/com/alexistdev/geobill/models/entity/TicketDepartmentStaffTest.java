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
public class TicketDepartmentStaffTest {

    private UUID id;
    private UUID departmentId;
    private UUID staffId;
    private TicketDepartmentStaff departmentStaff;
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
        staffId = UUID.randomUUID();

        TicketDepartment department = new TicketDepartment();
        department.setId(departmentId);

        User staff = new User();
        staff.setId(staffId);
        staff.setRole(Role.STAFF);

        departmentStaff = new TicketDepartmentStaff();
        departmentStaff.setId(id);
        departmentStaff.setDepartment(department);
        departmentStaff.setUser(staff);
        departmentStaff.setIsSupervisor(Boolean.TRUE);
        departmentStaff.setDeleted(false);
        departmentStaff.setCreatedBy("system");
        departmentStaff.setCreatedDate(new Date());
        departmentStaff.setModifiedBy("system");
        departmentStaff.setModifiedDate(new Date());
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get data")
    void testGetData() {
        Assertions.assertEquals(id, departmentStaff.getId());
        Assertions.assertEquals(departmentId, departmentStaff.getDepartment().getId());
        Assertions.assertEquals(staffId, departmentStaff.getUser().getId());
        Assertions.assertEquals(Role.STAFF, departmentStaff.getUser().getRole());
        Assertions.assertTrue(departmentStaff.getIsSupervisor());
        Assertions.assertNotNull(departmentStaff.getCreatedDate());
        Assertions.assertNotNull(departmentStaff.getModifiedDate());
        Assertions.assertNotNull(departmentStaff.getCreatedBy());
        Assertions.assertNotNull(departmentStaff.getModifiedBy());
        Assertions.assertFalse(departmentStaff.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        UUID newDepartmentId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();

        TicketDepartment newDepartment = new TicketDepartment();
        newDepartment.setId(newDepartmentId);
        User newUser = new User();
        newUser.setId(newUserId);
        newUser.setRole(Role.ADMIN);

        departmentStaff.setId(newId);
        departmentStaff.setDepartment(newDepartment);
        departmentStaff.setUser(newUser);
        departmentStaff.setIsSupervisor(Boolean.FALSE);

        Assertions.assertEquals(newId, departmentStaff.getId());
        Assertions.assertEquals(newDepartmentId, departmentStaff.getDepartment().getId());
        Assertions.assertEquals(newUserId, departmentStaff.getUser().getId());
        Assertions.assertEquals(Role.ADMIN, departmentStaff.getUser().getRole());
        Assertions.assertFalse(departmentStaff.getIsSupervisor());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure TicketDepartmentStaff is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, departmentStaff);
    }

    @Test
    @Order(4)
    @DisplayName("4. Should verify TicketDepartmentStaff extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, departmentStaff);
    }

    @Test
    @Order(5)
    @DisplayName("5. Should verify id is UUID")
    void testIdTypeIsUUID() {
        try {
            Field field = TicketDepartmentStaff.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (NoSuchFieldException e) {
            Assertions.fail("Field 'id' not found in TicketDepartmentStaff class");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Should not allow null value")
    void testNullNotAllowed() {
        departmentStaff.setDepartment(null);
        departmentStaff.setUser(null);
        departmentStaff.setIsSupervisor(null);

        Set<ConstraintViolation<TicketDepartmentStaff>> violations = validator.validate(departmentStaff);
        Assertions.assertEquals(3, violations.size(),
                "Should have exactly 3 validation errors for null mandatory fields");

        List<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        Assertions.assertTrue(violatedProperties.contains("department"), "Missing violation for 'department'");
        Assertions.assertTrue(violatedProperties.contains("user"), "Missing violation for 'user'");
        Assertions.assertTrue(violatedProperties.contains("isSupervisor"), "Missing violation for 'isSupervisor'");
    }

    @Test
    @Order(7)
    @DisplayName("7. Nilai default penugasan baru")
    void testDefaultValues() {
        TicketDepartmentStaff fresh = new TicketDepartmentStaff();

        Assertions.assertFalse(fresh.getIsSupervisor());
        Assertions.assertFalse(fresh.getDeleted());
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Equals and HashCode")
    void testEqualsAndHashCode() {
        TicketDepartmentStaff sameId = new TicketDepartmentStaff();
        sameId.setId(departmentStaff.getId());

        TicketDepartmentStaff otherId = new TicketDepartmentStaff();
        otherId.setId(UUID.randomUUID());

        Assertions.assertEquals(departmentStaff, sameId);
        Assertions.assertEquals(departmentStaff.hashCode(), sameId.hashCode());
        Assertions.assertNotEquals(departmentStaff, otherId);
    }
}
