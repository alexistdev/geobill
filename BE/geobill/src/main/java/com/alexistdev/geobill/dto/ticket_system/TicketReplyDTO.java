package com.alexistdev.geobill.dto.ticket_system;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TicketReplyDTO {

    private String id;
    private String ticketId;
    private String authorType;
    private String authorName;
    private String authorId;
    private String message;
    private boolean internalNote;
    private String createdDate;
    private List<TicketAttachmentDTO> attachments;
}
