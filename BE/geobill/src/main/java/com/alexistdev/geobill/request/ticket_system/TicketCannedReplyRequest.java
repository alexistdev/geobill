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
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "{ticketCannedReplyRequest.departmentId.uuid}")
    private String departmentId;

    @NotBlank(message = "{ticketCannedReplyRequest.title.required}")
    @Size(max = 150, message = "{ticketCannedReplyRequest.title.size}")
    private String title;

    @NotBlank(message = "{ticketCannedReplyRequest.body.required}")
    private String body;

    private Boolean isActive;

    @Min(value = 0, message = "{ticketCannedReplyRequest.sortOrder.min}")
    private Integer sortOrder;
}
