package com.alexistdev.geobill.request.ticket_system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketStaffRequest {

    @NotBlank(message = "{ticketStaffRequest.departmentId.required}")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "{ticketStaffRequest.departmentId.uuid}")
    private String departmentId;

    @NotBlank(message = "{ticketStaffRequest.userId.required}")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "{ticketStaffRequest.userId.uuid}")
    private String userId;

    private boolean supervisor;
}
