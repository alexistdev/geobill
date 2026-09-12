package com.alexistdev.geobill.dto.ticket_system;

import lombok.Getter;
import lombok.Setter;

/** Satu tab pada halaman ticket beserta jumlah tiketnya. */
@Getter
@Setter
public class TicketTabDTO {

    private String key;
    private String label;
    private long count;
}
