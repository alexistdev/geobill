package com.alexistdev.geobill.dto.ticket_system;

import lombok.Getter;
import lombok.Setter;

/** Satu pilihan pada dropdown Assigned To. */
@Getter
@Setter
public class TicketStaffDTO {

    private String id;
    private String fullName;
    private String email;
    private String role;
    private boolean supervisor;
}
