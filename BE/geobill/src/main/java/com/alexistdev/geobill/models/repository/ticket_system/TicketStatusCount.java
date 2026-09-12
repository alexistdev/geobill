package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.TicketStatus;

/**
 * Hasil hitungan tiket per status, dipakai mengisi angka pada setiap tab
 * di halaman ticket tanpa perlu satu query per tab.
 */
public interface TicketStatusCount {

    TicketStatus getStatus();

    Long getTotal();
}
