package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.Invoice;
import com.alexistdev.geobill.models.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface InvoiceRepo extends JpaRepository<Invoice, UUID> {
     boolean existsByInvoiceCode(String invoiceCode);
     Invoice findFirstByHostingOrderByCreatedDateDesc(Hosting hosting);

     @Query("SELECT inv FROM Invoice inv LEFT JOIN FETCH inv.hosting h WHERE inv.isDeleted = false AND h.isDeleted = false AND inv.user = :user")
     Page<Invoice> findByUserIdAndIsDeletedFalse(@Param("user") User user, Pageable pageable);
}
