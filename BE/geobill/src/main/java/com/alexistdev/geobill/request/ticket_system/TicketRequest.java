package com.alexistdev.geobill.request.ticket_system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketRequest {

    @NotBlank(message = "userId is required")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "userId must be a valid UUID")
    private String userId;

    @NotBlank(message = "departmentId is required")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "departmentId must be a valid UUID")
    private String departmentId;

    @NotBlank(message = "subject is required")
    @Size(max = 200, message = "subject must be less than 200 characters")
    private String subject;

    @NotBlank(message = "message is required")
    private String message;

    /** LOW, MEDIUM, HIGH, atau URGENT. Kosong berarti MEDIUM. */
    private String priority;

    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "hostingId must be a valid UUID")
    private String hostingId;

    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "invoiceId must be a valid UUID")
    private String invoiceId;

    @Size(max = 500, message = "ccEmails must be less than 500 characters")
    private String ccEmails;
}
