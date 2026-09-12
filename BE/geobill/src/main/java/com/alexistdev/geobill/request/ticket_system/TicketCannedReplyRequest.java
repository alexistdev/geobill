package com.alexistdev.geobill.request.ticket_system;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketCannedReplyRequest {

    /** Kosong berarti template berlaku untuk semua departemen. */
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "departmentId must be a valid UUID")
    private String departmentId;

    @NotBlank(message = "title is required")
    @Size(max = 150, message = "title must be less than 150 characters")
    private String title;

    @NotBlank(message = "body is required")
    private String body;

    private Boolean isActive;

    @Min(value = 0, message = "sortOrder must be zero or greater")
    private Integer sortOrder;
}
