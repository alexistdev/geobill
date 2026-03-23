package com.alexistdev.geobill.factory;

import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.Invoice;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.request.HostingRequest;
import com.alexistdev.geobill.services.ProductService;
import com.alexistdev.geobill.utils.InvoiceCodeGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InvoiceFactory {
    private final ProductService productService;
    private final InvoiceCodeGenerator invoiceCodeGenerator;

    public InvoiceFactory(ProductService productService, InvoiceCodeGenerator invoiceCodeGenerator) {
        this.productService = productService;
        this.invoiceCodeGenerator = invoiceCodeGenerator;
    }

    public Invoice createInvoice(Hosting hosting, HostingRequest hostingRequest) {
        UUID productId = UUID.fromString(hostingRequest.getProductId());
        Product product = productService.findEntityById(productId);

        if (product == null) {
            throw new NotFoundException("Product not found with ID: " + productId); // Replace with custom exception
        }

        int cycle = hostingRequest.getCycle();
        double subTotal = product.getPrice() * cycle;
        double tax = subTotal * 0; // Use tax calculator here
        double discount = subTotal * 0; // Use discount calculator here
        double total = subTotal + tax - discount;

        Invoice invoice = new Invoice();
        invoice.setUser(hosting.getUser());
        invoice.setHosting(hosting);
        invoice.setInvoiceCode(invoiceCodeGenerator.generateInvoiceCode());
        invoice.setDetail(generateDetail(hosting.getDomain()));
        invoice.setPrice(product.getPrice());
        invoice.setSubTotal(subTotal);
        invoice.setTotal(total);
        invoice.setCycle(cycle);
        invoice.setTax(tax);
        invoice.setDiscount(discount);
        invoice.setStartDate(hosting.getStartDate());
        invoice.setEndDate(hosting.getEndDate());
        invoice.setStatus(0);
        return invoice;
    }

    private String generateDetail(String domain) {
        return "Hosting service for " + domain;
    }

}
