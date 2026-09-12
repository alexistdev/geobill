package com.alexistdev.geobill.request.ticket_system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketRequest {

    /** Diisi controller dari pengguna yang sedang login, bukan dari kiriman klien. */
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "{ticketRequest.userId.uuid}")
    private String userId;

    @NotBlank(message = "{ticketRequest.departmentId.required}")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "{ticketRequest.departmentId.uuid}")
    private String departmentId;

    @NotBlank(message = "{ticketRequest.subject.required}")
    @Size(max = 200, message = "{ticketRequest.subject.size}")
    private String subject;

    @NotBlank(message = "{ticketRequest.message.required}")
    private String message;

    /** LOW, MEDIUM, HIGH, atau URGENT. Kosong berarti MEDIUM. */
    private String priority;

    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "{ticketRequest.hostingId.uuid}")
    private String hostingId;

    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "{ticketRequest.invoiceId.uuid}")
    private String invoiceId;

    @Size(max = 500, message = "{ticketRequest.ccEmails.size}")
    private String ccEmails;
}
