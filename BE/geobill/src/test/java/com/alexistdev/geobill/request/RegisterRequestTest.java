package com.alexistdev.geobill.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegisterRequestTest {

    @Test
    void testRegisterRequestGettersAndSetters() {
        //Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        String fullName = "testing";
        String email = "test@gmail.com";
        String password = "password123";

        //Act
        registerRequest.setFullName(fullName);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);

        //Assertions
        Assertions.assertEquals(fullName,registerRequest.getFullName());
        Assertions.assertEquals(email,registerRequest.getEmail());
        Assertions.assertEquals(password,registerRequest.getPassword());
    }

    @Test
    void testRegisterRequestDefaultConstructor() {
        //Arrange & Act
        RegisterRequest registerRequest = new RegisterRequest();

        //Assertions
        Assertions.assertNull(registerRequest.getFullName());
        Assertions.assertNull(registerRequest.getEmail());
        Assertions.assertNull(registerRequest.getPassword());
    }
}
