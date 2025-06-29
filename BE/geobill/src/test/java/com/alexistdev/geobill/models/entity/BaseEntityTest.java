package com.alexistdev.geobill.models.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class BaseEntityTest {

    private static class TestEntity extends BaseEntity<String> {
        // Concrete subclass for testing
    }

    private TestEntity testEntity;

    @BeforeEach
    void setUp() {
        testEntity = new TestEntity();
    }

    @Test
    void testCreatedBy() {
        String createdBy = "admin";
        testEntity.setCreatedBy(createdBy);
        Assertions.assertEquals(createdBy,testEntity.getCreatedBy());
    }

    @Test
    void testModifiedBy() {
        String modifiedBy = "admin2";
        testEntity.setModifiedBy(modifiedBy);
        Assertions.assertEquals(modifiedBy,testEntity.getModifiedBy());
    }

    @Test
    void testCreatedDate(){
        Date createdDate = new Date();
        testEntity.setCreatedDate(createdDate);
        Assertions.assertEquals(createdDate,testEntity.getCreatedDate());
    }

    @Test
    void testModifiedDate(){
        Date modifiedDate = new Date();
        testEntity.setModifiedDate(modifiedDate);
        Assertions.assertEquals(modifiedDate,testEntity.getModifiedDate());
    }

    @Test
    void testInitialValues(){
        Assertions.assertNull(testEntity.getCreatedBy());
        Assertions.assertNull(testEntity.getModifiedBy());
        Assertions.assertNull(testEntity.getCreatedDate());
        Assertions.assertNull(testEntity.getModifiedDate());
    }

    @Test
    void testNullValues(){
        testEntity.setCreatedBy(null);
        testEntity.setModifiedBy(null);
        testEntity.setCreatedDate(null);
        testEntity.setModifiedDate(null);

        Assertions.assertNull(testEntity.getCreatedBy());
        Assertions.assertNull(testEntity.getModifiedBy());
        Assertions.assertNull(testEntity.getCreatedDate());
        Assertions.assertNull(testEntity.getModifiedDate());
    }

}
