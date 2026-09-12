package com.alexistdev.geobill.dto.ticket_system;

import lombok.Getter;
import lombok.Setter;

/** Satu baris pada daftar tiket halaman admin. */
@Getter
@Setter
public class TicketDTO {

    private String id;
    private String number;
    private String summary;
    private String priority;
    private String department;
    private String departmentId;
    private String assignedTo;
    private String assignedToId;
    private String requester;
    private String lastReply;
    private String status;
    private int replyCount;
}
