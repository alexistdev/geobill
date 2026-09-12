package com.alexistdev.geobill.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HostingRequest {
    @NotBlank(message = "{hostingRequest.userId.required}")
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "{hostingRequest.userId.uuid}"
    )
    private String userId;

    @NotBlank(message = "{hostingRequest.productId.required}")
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "{hostingRequest.productId.uuid}"
    )
    private String productId;

    @NotBlank(message = "{hostingRequest.domainName.required}")
    private String domainName;

    @NotNull(message = "{hostingRequest.price.required}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{hostingRequest.price.min}")
    private Double price;

    @Min(value = 1, message = "{hostingRequest.cycle.min}")
    private int cycle;
}
