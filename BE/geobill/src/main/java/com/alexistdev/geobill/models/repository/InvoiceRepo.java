package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceRepo extends JpaRepository<Invoice, UUID> {
     boolean existsByInvoiceCode(String invoiceCode);
}
