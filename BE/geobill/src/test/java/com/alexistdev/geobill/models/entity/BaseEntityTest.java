package com.alexistdev.geobill.models.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Test CreatedBy")
    void testCreatedBy() {
        String createdBy = "admin";
        testEntity.setCreatedBy(createdBy);
        Assertions.assertEquals(createdBy,testEntity.getCreatedBy());
    }

    @Test
    @DisplayName("Test ModifiedBy")
    void testModifiedBy() {
        String modifiedBy = "admin2";
        testEntity.setModifiedBy(modifiedBy);
        Assertions.assertEquals(modifiedBy,testEntity.getModifiedBy());
    }

    @Test
    @DisplayName("Test CreatedDate")
    void testCreatedDate(){
        Date createdDate = new Date();
        testEntity.setCreatedDate(createdDate);
        Assertions.assertEquals(createdDate,testEntity.getCreatedDate());
    }

    @Test
    @DisplayName("Test ModifiedDate")
    void testModifiedDate(){
        Date modifiedDate = new Date();
        testEntity.setModifiedDate(modifiedDate);
        Assertions.assertEquals(modifiedDate,testEntity.getModifiedDate());
    }

    @Test
    @DisplayName("Test Initial Values")
    void testInitialValues(){
        Assertions.assertNull(testEntity.getCreatedBy());
        Assertions.assertNull(testEntity.getModifiedBy());
        Assertions.assertNull(testEntity.getCreatedDate());
        Assertions.assertNull(testEntity.getModifiedDate());
    }

    @Test
    @DisplayName("Test Null Values")
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

    @Test
    @DisplayName("Test Deleted")
    public void testSetAndGetDeleted(){
        Assertions.assertFalse(testEntity.getDeleted());

        testEntity.setDeleted(true);
        Assertions.assertTrue(testEntity.getDeleted());

        testEntity.setDeleted(false);
        Assertions.assertFalse(testEntity.getDeleted());

        testEntity.setDeleted(null);
        Assertions.assertNull(testEntity.getDeleted());
    }


}
