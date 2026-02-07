package com.alexistdev.geobill.models.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

public class CustomerTest {

    private static Validator validator;
    private Customer customer;
    private String business_name;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String country;
    private String post_code;
    private String phone;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        business_name = "business name";
        address1 = "address1";
        address2 = "address2";
        city = "city";
        state = "state";
        country = "country";
        post_code = "post code";
        phone = "08123456789";

        customer = new Customer();
        customer.setBusinessName(business_name);
        customer.setAddress1(address1);
        customer.setAddress2(address2);
        customer.setCity(city);
        customer.setState(state);
        customer.setCountry(country);
        customer.setPostCode(post_code);
        customer.setPhone(phone);
    }

    @Test
    @Order(1)
    @DisplayName( "1. Test Getter")
    void testGetter() {
        Assertions.assertEquals(business_name,customer.getBusinessName());
        Assertions.assertEquals(address1,customer.getAddress1());
        Assertions.assertEquals(address2,customer.getAddress2());
        Assertions.assertEquals(city,customer.getCity());
        Assertions.assertEquals(state,customer.getState());
        Assertions.assertEquals(country,customer.getCountry());
        Assertions.assertEquals(post_code,customer.getPostCode());
        Assertions.assertEquals(phone,customer.getPhone());
    }

    @Test
    @Order(2)
    @DisplayName( "2. Test Setter")
    void testSetter() {
        String newFirstName ="new first name";
        String newLastName = "new last name";
        String newBusinessName = "new business name";
        String newAddress1 = "new address1";
        String newAddress2 = "new address2";
        String newCity = "new city";
        String newState = "new state";
        String newCountry = "new country";
        String newPostCode = "new post code";
        String newPhone = "021-1234567";

        customer.setBusinessName(newBusinessName);
        customer.setAddress1(newAddress1);
        customer.setAddress2(newAddress2);
        customer.setCity(newCity);
        customer.setState(newState);
        customer.setCountry(newCountry);
        customer.setPostCode(newPostCode);
        customer.setPhone(newPhone);

        Assertions.assertEquals(newBusinessName,customer.getBusinessName());
        Assertions.assertEquals(newAddress1,customer.getAddress1());
        Assertions.assertEquals(newAddress2,customer.getAddress2());
        Assertions.assertEquals(newCity,customer.getCity());
        Assertions.assertEquals(newState,customer.getState());
        Assertions.assertEquals(newCountry,customer.getCountry());
        Assertions.assertEquals(newPostCode,customer.getPostCode());
        Assertions.assertEquals(newPhone,customer.getPhone());
    }

    @Test
    @Order(3)
    @DisplayName("3. Should fail validation when user is null")
    void shouldFailValidationWhenUserIsNull() {
        customer.setUser(null);
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should pass validation when user is present")
    void shouldPassValidationWhenUserIsPresent() {
        customer.setUser(new User());
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("5. Customer should be Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(Serializable.class, customer);
    }

    @Test
    @Order(6)
    @DisplayName("6. Should verify id is UUID")
    void testIdTypeIsUUID() throws NoSuchFieldException {
        Field field = Customer.class.getDeclaredField("id");
        field.setAccessible(true);
        Assertions.assertEquals(UUID.class, field.getType());
    }


}
