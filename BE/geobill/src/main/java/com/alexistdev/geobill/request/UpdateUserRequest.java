package com.alexistdev.geobill.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @NotBlank(message = "Full Name is required")
    @Size(max = 150, message = "Full Name must be less than 150 characters")
    private String fullName;

    @Nullable
    @Size(max = 16, message = "Phone Number must be 16 digits")
    private String phoneNumber;

    @Nullable
    @Size(max = 150, message = "Business Name must be less than 150 characters")
    private String businessName;

    @Nullable
    @Size(max = 255, message = "Address1 must be less than 255 characters")
    private String address1;

    @Nullable
    @Size(max = 255, message = "Address2 must be less than 255 characters")
    private String address2;

    @Nullable
    @Size(max = 50, message = "City must be less than 50 characters")
    private String city;

    @Nullable
    @Size(max = 50, message = "State must be less than 50 characters")
    private String state;

    @Nullable
    @Size(max = 50, message = "Country must be less than 50 characters")
    private String country;

    @Nullable
    @Size(max = 10, message = "Post Code must be less than 10 characters")
    private String postCode;
}
