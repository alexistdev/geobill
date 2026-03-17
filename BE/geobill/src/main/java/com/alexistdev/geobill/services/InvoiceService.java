package com.alexistdev.geobill.services;

import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.Invoice;
import com.alexistdev.geobill.models.repository.InvoiceRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class InvoiceService {

    public final InvoiceRepo invoiceRepo;

    public InvoiceService(InvoiceRepo invoiceRepo) {
        this.invoiceRepo = invoiceRepo;
    }

    @Transactional
    public Invoice createInvoice(Hosting hosting){
        Invoice invoice = new Invoice();
        invoice.setUser(hosting.getUser());
        invoice.setHosting(hosting);
        invoice.setInvoiceCode(generateInvoiceCode());
        invoice.setDetail("Hosting service for " + hosting.getDomain());
        invoice.setSubTotal(hosting.getPrice());
        invoice.setTotal(hosting.getPrice());
        invoice.setTax(0.0);
        invoice.setDiscount(0.0);
        invoice.setStartDate(hosting.getStartDate());
        invoice.setEndDate(hosting.getEndDate());
        invoice.setStatus(0);
        return invoiceRepo.save(invoice);
    }

    private String generateInvoiceCode() {
        String code;
        do {
            String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            code = "INV-" + randomStr;
        } while (invoiceRepo.existsByInvoiceCode(code));
        return code;
    }
}
