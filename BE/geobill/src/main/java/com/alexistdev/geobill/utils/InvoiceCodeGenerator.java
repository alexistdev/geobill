package com.alexistdev.geobill.utils;

import com.alexistdev.geobill.models.repository.InvoiceRepo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InvoiceCodeGenerator {
    private final InvoiceRepo invoiceRepo;

    public InvoiceCodeGenerator(InvoiceRepo invoiceRepo) {
        this.invoiceRepo = invoiceRepo;
    }

    public String generateInvoiceCode() {
        String code;
        do {
            String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            code = "INV-" + randomStr;
        } while (invoiceRepo.existsByInvoiceCode(code));
        return code;
    }
}
