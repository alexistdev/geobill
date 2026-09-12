package com.alexistdev.geobill.request.ticket_system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketReplyRequest {

    /** Diisi controller dari path, bukan dari kiriman klien. */
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "ticketId must be a valid UUID")
    private String ticketId;

    /** Diisi controller dari pengguna yang sedang login, bukan dari kiriman klien. */
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", message = "userId must be a valid UUID")
    private String userId;

    @NotBlank(message = "message is required")
    private String message;

    /** Catatan internal tidak pernah dikirim ke klien dan tidak mengubah status tiket. */
    private boolean internalNote;
}
