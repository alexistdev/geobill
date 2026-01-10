package com.alexistdev.geobill.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProductTypeDTOTest {

    private ProductTypeDTO productTypeDTO;

    @BeforeEach
    void setUp() {
        productTypeDTO = new ProductTypeDTO();
    }

    @Test
    @DisplayName( "Test Set and Get Id")
    void testSetAndGetId() {
        String testId = "123";
        productTypeDTO.setId(testId);
        Assertions.assertEquals(testId, productTypeDTO.getId());
    }

    @Test
    @DisplayName( "Test Set and Get Name")
    void testSetAndGetName() {
        String testName = "VPS";
        productTypeDTO.setName(testName);
        Assertions.assertEquals(testName, productTypeDTO.getName());
    }

    @Test
    @DisplayName( "Test Null Values")
    void testNullValues() {
        productTypeDTO.setId(null);
        productTypeDTO.setName(null);
        Assertions.assertNull(productTypeDTO.getId());
        Assertions.assertNull(productTypeDTO.getName());
    }
}
