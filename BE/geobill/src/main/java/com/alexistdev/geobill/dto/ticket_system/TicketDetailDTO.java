package com.alexistdev.geobill.dto.ticket_system;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Satu tiket beserta percakapannya. */
@Getter
@Setter
public class TicketDetailDTO {

    private String id;
    private String number;
    private String subject;
    private String priority;
    private String status;
    private String source;
    private String department;
    private String departmentId;
    private String requester;
    private String requesterId;
    private String assignedTo;
    private String assignedToId;
    private String hostingId;
    private String invoiceId;
    private String ccEmails;
    private String createdDate;
    private String lastReply;
    private String closedDate;
    private Integer rating;
    private boolean flagged;
    private List<TicketReplyDTO> replies;
    private List<TicketAttachmentDTO> attachments;
}
