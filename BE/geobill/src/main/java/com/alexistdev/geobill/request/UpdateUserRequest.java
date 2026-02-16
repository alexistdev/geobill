package com.alexistdev.geobill.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @NonNull
    @NotBlank(message = "Id is required")
    private String id;

    @NotBlank(message = "Full Name is required")
    @Size(max=150, message = "Full Name must be less than 150 characters")
    private String FullName;

    @NotBlank(message = "Email is required")
    @Size(max=150, message = "Email must be less than 150 characters")
    @Email(message = "Email is not valid")
    private String Email;

    @Nullable
    @Size(max=16, message = "Phone Number must be 16 digits")
    private String PhoneNumber;

    @Nullable
    @Size(max=150, message = "Business Name must be less than 150 characters")
    private String BusinessName;

    @Nullable
    @Size(max=255, message = "Address1 must be less than 255 characters")
    private String Address1;

    @Nullable
    @Size(max=255, message = "Address2 must be less than 255 characters")
    private String Address2;

    @Nullable
    @Size(max=50, message = "City must be less than 50 characters")
    private String City;

    @Nullable
    @Size(max=50, message = "State must be less than 50 characters")
    private String State;

    @Nullable
    @Size(max=50, message = "Country must be less than 50 characters")
    private String Country;

    @Nullable
    @Size(max=10, message = "Post Code must be less than 10 characters")
    private String PostCode;

}
