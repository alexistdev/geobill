package com.alexistdev.geobill.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @NotBlank(message = "{updateUserRequest.fullName.required}")
    @Size(max = 150, message = "{updateUserRequest.fullName.size}")
    private String fullName;

    @Nullable
    @Size(max = 16, message = "{updateUserRequest.phoneNumber.size}")
    private String phoneNumber;

    @Nullable
    @Size(max = 150, message = "{updateUserRequest.businessName.size}")
    private String businessName;

    @Nullable
    @Size(max = 255, message = "{updateUserRequest.address1.size}")
    private String address1;

    @Nullable
    @Size(max = 255, message = "{updateUserRequest.address2.size}")
    private String address2;

    @Nullable
    @Size(max = 50, message = "{updateUserRequest.city.size}")
    private String city;

    @Nullable
    @Size(max = 50, message = "{updateUserRequest.state.size}")
    private String state;

    @Nullable
    @Size(max = 50, message = "{updateUserRequest.country.size}")
    private String country;

    @Nullable
    @Size(max = 10, message = "{updateUserRequest.postCode.size}")
    private String postCode;
}
