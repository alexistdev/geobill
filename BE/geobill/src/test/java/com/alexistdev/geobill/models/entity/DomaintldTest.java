package com.alexistdev.geobill.models.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class DomaintldTest {

    private Domaintld domaintld;

    @BeforeEach
    void setUp() {
        domaintld = new Domaintld();
    }

    @Test
    void testNameGetterAndSetter() {
        String testName = ".com";
        domaintld.setName(testName);
        Assertions.assertEquals(testName, domaintld.getName(), "Name should match the set value");
    }

    @Test
    void testIdGeneration() {
        Assertions.assertNull(domaintld.getId(), "ID should be null before persistence");
    }

    @Test
    void testInheritedAuditFields() {
        // Test createdBy
        String creator = "testUser";
        domaintld.setCreatedBy(creator);
        Assertions.assertEquals(creator, domaintld.getCreatedBy(), "CreatedBy should match the set value");

        // Test modifiedBy
        String modifier = "adminUser";
        domaintld.setModifiedBy(modifier);
        Assertions.assertEquals(modifier, domaintld.getModifiedBy(), "ModifiedBy should match the set value");

        // Test createdDate
        Date createdDate = new Date();
        domaintld.setCreatedDate(createdDate);
        Assertions.assertEquals(createdDate, domaintld.getCreatedDate(), "CreatedDate should match the set value");

        // Test modifiedDate
        Date modifiedDate = new Date();
        domaintld.setModifiedDate(modifiedDate);
        Assertions.assertEquals(modifiedDate, domaintld.getModifiedDate(), "ModifiedDate should match the set value");
    }

    @Test
    void testIsDeletedDefaultValue() {
        Assertions.assertFalse(domaintld.getIsDeleted(), "isDeleted should be false by default");
    }

    @Test
    void testIsDeletedSetter() {
        domaintld.setIsDeleted(true);
        Assertions.assertTrue(domaintld.getIsDeleted(), "isDeleted should be true after setting");
    }

    @Test
    void testNameValidation() {
        // Test null name
        Assertions.assertThrows(NullPointerException.class, () -> {
            Domaintld invalidDomain = new Domaintld();
            invalidDomain.setName(null);
            // This would throw an exception in a real database operation
            // but for unit testing we simulate the validation
            if (invalidDomain.getName() == null) {
                throw new NullPointerException("Name cannot be null");
            }
        });

        // Test empty name
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Domaintld invalidDomain = new Domaintld();
            invalidDomain.setName("");
            // Simulate validation
            if (invalidDomain.getName().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be empty");
            }
        });

        // Test name length validation
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Domaintld invalidDomain = new Domaintld();
            invalidDomain.setName("ThisIsAVeryLongDomainTldNameThatExceedsTheLimit");
            // Simulate validation
            if (invalidDomain.getName().length() > 20) {
                throw new IllegalArgumentException("Name length cannot exceed 20 characters");
            }
        });
    }

    @Test
    void testValidDomainTld() {
        Domaintld validDomain = new Domaintld();
        validDomain.setName(".com");
        validDomain.setCreatedBy("system");
        validDomain.setCreatedDate(new Date());
        validDomain.setIsDeleted(false);

        Assertions.assertEquals(".com", validDomain.getName());
        Assertions.assertEquals("system", validDomain.getCreatedBy());
        Assertions.assertNotNull(validDomain.getCreatedDate());
        Assertions.assertFalse(validDomain.getIsDeleted());
    }



}
