package com.alexistdev.geobill.utils;

import com.alexistdev.geobill.models.repository.ticket_system.TicketRepo;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Nomor tiket berformat TKT-YYYYMM-000001, berurut dalam satu bulan.
 * Nomor yang sudah dipakai tiket terhapus tidak boleh dipakai ulang, karena kolomnya
 * tetap unik di database walau barisnya sudah di-soft-delete.
 */
@Component
public class TicketCodeGenerator {

    private static final String PREFIX = "TKT-";
    private static final String MONTH_PATTERN = "yyyyMM";
    private static final String SEQUENCE_PATTERN = "%06d";

    private final TicketRepo ticketRepo;

    public TicketCodeGenerator(TicketRepo ticketRepo) {
        this.ticketRepo = ticketRepo;
    }

    public String generateTicketCode() {
        String prefix = PREFIX + new SimpleDateFormat(MONTH_PATTERN).format(new Date()) + "-";
        long sequence = ticketRepo.countByTicketNumberPrefixIncludingDeleted(prefix) + 1;

        String code;
        do {
            code = prefix + String.format(SEQUENCE_PATTERN, sequence);
            sequence++;
        } while (ticketRepo.existsByTicketNumberIncludingDeleted(code));
        return code;
    }
}
