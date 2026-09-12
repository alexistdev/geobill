package com.alexistdev.geobill.request.ticket_system;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketDepartmentRequest {

    @NotBlank(message = "{ticketDepartmentRequest.name.required}")
    @Size(max = 100, message = "{ticketDepartmentRequest.name.size}")
    private String name;

    @NotBlank(message = "{ticketDepartmentRequest.code.required}")
    @Size(max = 50, message = "{ticketDepartmentRequest.code.size}")
    private String code;

    @Size(max = 150, message = "{ticketDepartmentRequest.email.size}")
    private String email;

    @Size(max = 255, message = "{ticketDepartmentRequest.description.size}")
    private String description;

    @Min(value = 0, message = "{ticketDepartmentRequest.sortOrder.min}")
    private Integer sortOrder;

    private Boolean isActive;

    @Min(value = 1, message = "{ticketDepartmentRequest.autoCloseDays.min}")
    private Integer autoCloseDays;
}
