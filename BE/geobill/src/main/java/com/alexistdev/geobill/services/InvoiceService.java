package com.alexistdev.geobill.services;

import com.alexistdev.geobill.dto.InvoiceUserDTO;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.Invoice;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.InvoiceRepo;
import com.alexistdev.geobill.utils.MessagesUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InvoiceService {

    public final InvoiceRepo invoiceRepo;
    public final MessagesUtils messagesUtils;

    public InvoiceService(InvoiceRepo invoiceRepo, MessagesUtils messagesUtils) {
        this.invoiceRepo = invoiceRepo;
        this.messagesUtils = messagesUtils;
    }

    public Page<InvoiceUserDTO> getAllInvoicesByUser(Pageable pageable, User user){
        Page<Invoice> result = invoiceRepo.findByUserIdAndIsDeletedFalse(user, pageable);

        List<InvoiceUserDTO> invoiceDTOs = result.getContent().stream()
                .map(this::convertToInvoiceUserDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(invoiceDTOs, pageable, result.getTotalElements());
    }

    public InvoiceUserDTO getInvoiceById(String id){
        Invoice invoice = invoiceRepo.findById(UUID.fromString(id)).orElse(null);

        if(invoice == null){
            log.error("Invoice not found with ID: {}", id);
            String notFoundMessage = messagesUtils.getMessage("invoiceservice.invoice_not_found", id);
            throw new NotFoundException(notFoundMessage);
        }

        return convertToInvoiceUserDTO(invoice);
    }

    private InvoiceUserDTO convertToInvoiceUserDTO(Invoice invoice){
        InvoiceUserDTO invoiceUserDTO = new InvoiceUserDTO();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String startDate = dateFormat.format(invoice.getStartDate());
        String endDate = dateFormat.format(invoice.getEndDate());

        invoiceUserDTO.setId(invoice.getId().toString());
        if (invoice.getHosting() != null && invoice.getHosting().getId() != null) {
            invoiceUserDTO.setHostingId(invoice.getHosting().getId().toString());
        }
        invoiceUserDTO.setInvoiceCode(invoice.getInvoiceCode());
        invoiceUserDTO.setDetail(invoice.getDetail());
        invoiceUserDTO.setSubTotal(invoice.getSubTotal());
        invoiceUserDTO.setTotal(invoice.getTotal());
        invoiceUserDTO.setTax(invoice.getTax());
        invoiceUserDTO.setDiscount(invoice.getDiscount());
        invoiceUserDTO.setCycle(invoice.getCycle());
        invoiceUserDTO.setStartDate(startDate);
        invoiceUserDTO.setEndDate(endDate);
        invoiceUserDTO.setStatus(invoice.getStatus());
        return invoiceUserDTO;
    }

    @Transactional
    public Invoice createInvoice(Hosting hosting, int cycle){
        Invoice invoice = new Invoice();
        invoice.setUser(hosting.getUser());
        invoice.setHosting(hosting);
        invoice.setInvoiceCode(generateInvoiceCode());
        invoice.setDetail(this.generateDetail(hosting.getDomain()));
        invoice.setSubTotal(hosting.getPrice());
        invoice.setTotal(hosting.getPrice());
        invoice.setCycle(cycle);
        invoice.setTax(0.0);
        invoice.setDiscount(0.0);
        invoice.setStartDate(hosting.getStartDate());
        invoice.setEndDate(hosting.getEndDate());
        invoice.setStatus(0);
        return invoiceRepo.save(invoice);
    }

    public Invoice findLatestInvoiceByHosting(Hosting hosting) {
        return invoiceRepo.findFirstByHostingOrderByCreatedDateDesc(hosting);
    }

    private String generateDetail(String domain) {
        return "Hosting service for " + domain;
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
