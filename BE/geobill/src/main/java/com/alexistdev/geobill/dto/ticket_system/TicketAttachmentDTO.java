package com.alexistdev.geobill.dto.ticket_system;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketAttachmentDTO {

    private String id;
    private String ticketId;
    private String replyId;
    private String name;
    private String mimeType;
    private Long fileSize;
    private String uploadedBy;
    private String createdDate;
}
