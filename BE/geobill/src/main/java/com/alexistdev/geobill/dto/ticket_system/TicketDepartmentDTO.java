package com.alexistdev.geobill.dto.ticket_system;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketDepartmentDTO {

    private String id;
    private String name;
    private String code;
    private String email;
    private String description;
    private Integer sortOrder;
    private boolean active;
    private Integer autoCloseDays;
}
