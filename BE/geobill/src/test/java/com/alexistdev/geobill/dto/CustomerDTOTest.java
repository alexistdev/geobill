package com.alexistdev.geobill.dto;

import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerDTOTest {
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customerDTO = new CustomerDTO();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Set and Get Id")
    void testSetAndGetId() {
        String testId = "123";
        customerDTO.setId(testId);
        Assertions.assertEquals(testId, customerDTO.getId());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Set and Get")
    void testSetAndGet() {
        String testBusinessName = "test";
        String testAddress1 = "testAddress1";
        String testAddress2 = "testAddress2";
        String testCity = "testCity";
        String testState = "testState";
        String testCountry = "testCountry";
        String testPostCode = "testPostCode";
        String testPhone = "testPhone";

        customerDTO.setBusinessName(testBusinessName);
        customerDTO.setAddress1(testAddress1);
        customerDTO.setAddress2(testAddress2);
        customerDTO.setCity(testCity);
        customerDTO.setState(testState);
        customerDTO.setCountry(testCountry);
        customerDTO.setPostCode(testPostCode);
        customerDTO.setPhone(testPhone);

        Assertions.assertEquals(testBusinessName, customerDTO.getBusinessName());
        Assertions.assertEquals(testAddress1, customerDTO.getAddress1());
        Assertions.assertEquals(testAddress2, customerDTO.getAddress2());
        Assertions.assertEquals(testCity, customerDTO.getCity());
        Assertions.assertEquals(testState, customerDTO.getState());
        Assertions.assertEquals(testCountry, customerDTO.getCountry());
        Assertions.assertEquals(testPostCode, customerDTO.getPostCode());
        Assertions.assertEquals(testPhone, customerDTO.getPhone());

    }

    @Test
    @Order(3)
    @DisplayName("3. Test Null Values")
    void testNullValues() {
        customerDTO.setId(null);
        customerDTO.setBusinessName(null);
        customerDTO.setAddress1(null);
        customerDTO.setAddress2(null);
        customerDTO.setCity(null);
        customerDTO.setState(null);
        customerDTO.setCountry(null);
        customerDTO.setPostCode(null);
        customerDTO.setPhone(null);

        Assertions.assertNull(customerDTO.getId());
        Assertions.assertNull(customerDTO.getBusinessName());
        Assertions.assertNull(customerDTO.getAddress1());
        Assertions.assertNull(customerDTO.getAddress2());
        Assertions.assertNull(customerDTO.getCity());
        Assertions.assertNull(customerDTO.getState());
        Assertions.assertNull(customerDTO.getCountry());
        Assertions.assertNull(customerDTO.getPostCode());
        Assertions.assertNull(customerDTO.getPhone());
    }
}
