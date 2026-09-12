package com.alexistdev.geobill.request.ticket_system;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketDepartmentRequest {

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be less than 100 characters")
    private String name;

    @NotBlank(message = "code is required")
    @Size(max = 50, message = "code must be less than 50 characters")
    private String code;

    @Size(max = 150, message = "email must be less than 150 characters")
    private String email;

    @Size(max = 255, message = "description must be less than 255 characters")
    private String description;

    @Min(value = 0, message = "sortOrder must be zero or greater")
    private Integer sortOrder;

    private Boolean isActive;

    @Min(value = 1, message = "autoCloseDays must be at least 1 day")
    private Integer autoCloseDays;
}
