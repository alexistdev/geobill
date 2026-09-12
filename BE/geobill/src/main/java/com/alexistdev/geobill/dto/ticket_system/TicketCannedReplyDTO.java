package com.alexistdev.geobill.dto.ticket_system;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketCannedReplyDTO {

    private String id;
    private String departmentId;
    private String department;
    private String title;
    private String body;
    private boolean active;
    private Integer sortOrder;
}
