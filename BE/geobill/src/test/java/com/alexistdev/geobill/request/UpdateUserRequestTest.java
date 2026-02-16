package com.alexistdev.geobill.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.util.Set;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UpdateUserRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Getter And Setter")
    void testGetterAndSetter() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        String testId = UUID.randomUUID().toString();
        String testFullName ="test fullname";
        String testEmail = "test@gmail.com";
        String testPhoneNumber = "08123456789";
        String testBussinessName = "test business name";
        String testAddress1 = "test address 1";
        String testAddress2 = "test address 2";
        String testCity = "test city";
        String testState = "test state";
        String testCountry = "test country";
        String testPostCode = "12345";

        updateUserRequest.setId(testId);
        updateUserRequest.setFullName(testFullName);
        updateUserRequest.setEmail(testEmail);
        updateUserRequest.setPhoneNumber(testPhoneNumber);
        updateUserRequest.setBusinessName(testBussinessName);
        updateUserRequest.setAddress1(testAddress1);
        updateUserRequest.setAddress2(testAddress2);
        updateUserRequest.setCity(testCity);
        updateUserRequest.setState(testState);
        updateUserRequest.setCountry(testCountry);
        updateUserRequest.setPostCode(testPostCode);

        Assertions.assertEquals(testId, updateUserRequest.getId());
        Assertions.assertEquals(testFullName, updateUserRequest.getFullName());
        Assertions.assertEquals(testEmail, updateUserRequest.getEmail());
        Assertions.assertEquals(testPhoneNumber, updateUserRequest.getPhoneNumber());
        Assertions.assertEquals(testBussinessName, updateUserRequest.getBusinessName());
        Assertions.assertEquals(testAddress1, updateUserRequest.getAddress1());
        Assertions.assertEquals(testAddress2, updateUserRequest.getAddress2());
        Assertions.assertEquals(testCity, updateUserRequest.getCity());
        Assertions.assertEquals(testState, updateUserRequest.getState());
        Assertions.assertEquals(testCountry, updateUserRequest.getCountry());
        Assertions.assertEquals(testPostCode, updateUserRequest.getPostCode());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Default Constructor")
    void testDefaultConstructor() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        Assertions.assertNull(updateUserRequest.getFullName());
        Assertions.assertNull(updateUserRequest.getEmail());
        Assertions.assertNull(updateUserRequest.getPhoneNumber());
        Assertions.assertNull(updateUserRequest.getBusinessName());
        Assertions.assertNull(updateUserRequest.getAddress1());
        Assertions.assertNull(updateUserRequest.getAddress2());
        Assertions.assertNull(updateUserRequest.getCity());
        Assertions.assertNull(updateUserRequest.getState());
        Assertions.assertNull(updateUserRequest.getCountry());
        Assertions.assertNull(updateUserRequest.getPostCode());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Validation")
    void testValidation() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(updateUserRequest);
        Assertions.assertEquals(3, violations.size());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Full Name Size Validation")
    void testFullNameSizeValidation() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        String maxFullName = "a".repeat(151);
        updateUserRequest.setFullName(maxFullName);

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(updateUserRequest);
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("FullName")));
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Email Size Validation")
    void testEmailSizeValidation() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        String maxEmail = "a".repeat(141);
        maxEmail += "@gmail.com";
        updateUserRequest.setEmail(maxEmail);
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(updateUserRequest);
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("Email")));

    }

    @Test
    @Order(6)
    @DisplayName("6. Test Email Format Validation")
    void testEmailFormatValidation() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setEmail("invalid-email");
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(updateUserRequest);
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("Email")
                && violation.getMessage().equals("Email is not valid")));
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Phone Number Size Validation")
    void testPhoneNumberSizeValidation() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        String maxPhoneNumber  = "0".repeat(17);
        updateUserRequest.setPhoneNumber(maxPhoneNumber);
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(updateUserRequest);
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("PhoneNumber")));
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Business Name Size Validation")
    void testBusinessNameSizeValidation() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        String maxBusinessName = "a".repeat(151);
        updateUserRequest.setBusinessName(maxBusinessName);
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(updateUserRequest);
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("BusinessName")));
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Address1 and Address2 Size Validation")
    void testAddressSizeValidation() {
        UpdateUserRequest updateUserRequest1= new UpdateUserRequest();
        UpdateUserRequest updateUserRequest2 = new UpdateUserRequest();
        String maxAddress = "a".repeat(256);
        updateUserRequest1.setAddress1(maxAddress);
        updateUserRequest2.setAddress2(maxAddress);
        Set<ConstraintViolation<UpdateUserRequest>> violations1 = validator.validate(updateUserRequest1);
        Set<ConstraintViolation<UpdateUserRequest>> violations2 = validator.validate(updateUserRequest2);
        Assertions.assertTrue(violations1.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("Address1")));

        Assertions.assertTrue(violations2.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("Address2")));
    }

    @Test
    @Order(10)
    @DisplayName("10. Test City Size Validation")
    void testCitySizeValidation() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        String maxCity = "a".repeat(51);
        updateUserRequest.setCity(maxCity);
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(updateUserRequest);
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("City")));
    }

    @Test
    @Order(11)
    @DisplayName("11. Test State Size Validation")
    void testStateSizeValidation() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        String maxState = "a".repeat(51);
        updateUserRequest.setState(maxState);
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(updateUserRequest);
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("State")));
    }

    @Test
    @Order(12)
    @DisplayName("12. Test Country Size Validation")
    void testCountrySizeValidation() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        String maxCountry = "a".repeat(51);
        updateUserRequest.setCountry(maxCountry);
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(updateUserRequest);
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("Country")));
    }

    @Test
    @Order(13)
    @DisplayName("13. Test Post Code Size Validation")
    void testPostCodeSizeValidation() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        String maxPostCode = "a".repeat(11);
        updateUserRequest.setPostCode(maxPostCode);
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(updateUserRequest);
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("PostCode")));
    }
}
