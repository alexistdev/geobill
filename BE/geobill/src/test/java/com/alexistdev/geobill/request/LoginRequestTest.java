package com.alexistdev.geobill.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoginRequestTest {

    @Test
    void testLoginRequestGettersAndSetters() {
        //Arrange
        LoginRequest loginRequest = new LoginRequest();
        String testEmail = "test@gmail.com";
        String testPassword = "password123";

        //Act
        loginRequest.setEmail(testEmail);
        loginRequest.setPassword(testPassword);

        //Assertion
        Assertions.assertEquals(testEmail,loginRequest.getEmail());
        Assertions.assertEquals(testPassword, loginRequest.getPassword());
    }

    @Test
    void testLoginRequestDefaultConstructor() {
        //Arrange & Act
        LoginRequest loginRequest = new LoginRequest();

        //Assertions
        Assertions.assertNull(loginRequest.getEmail());
        Assertions.assertNull(loginRequest.getPassword());
    }
}
