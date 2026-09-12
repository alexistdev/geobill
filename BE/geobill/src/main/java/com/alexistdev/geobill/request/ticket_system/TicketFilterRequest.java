package com.alexistdev.geobill.request.ticket_system;

import lombok.Getter;
import lombok.Setter;

/**
 * Isi panel filter pada halaman ticket. Setiap nilai yang kosong atau "Any"
 * diabaikan, sehingga filter kosong berarti seluruh tiket pada tab tersebut.
 */
@Getter
@Setter
public class TicketFilterRequest {

    private String ticketNumber;
    private String summary;
    private String priority;
    private String departmentId;
    private String assignedTo;

    /** Any, Today, Last 7 days, atau Last 30 days. */
    private String lastReply;
}
