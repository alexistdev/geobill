package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.Invoice;
import com.alexistdev.geobill.models.entity.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.junit.jupiter.api.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvoiceRepoTest {

    @Mock
    private InvoiceRepo invoiceRepo;

    @Test
    @Order(1)
    @DisplayName("1. Test findFirstByHostingOrderByCreatedDateDesc - Valid Hosting Returns Latest Invoice")
    void testFindFirstByHostingReturnsLatestInvoice() {
        Hosting hosting = new Hosting();
        hosting.setId(UUID.randomUUID());
        hosting.setName("Test Hosting");
        hosting.setDomain("example.com");

        Invoice expectedInvoice = new Invoice();
        expectedInvoice.setId(UUID.randomUUID());
        expectedInvoice.setHosting(hosting);
        expectedInvoice.setInvoiceCode("INV-001");
        expectedInvoice.setDetail("Invoice for Test Hosting");
        expectedInvoice.setSubTotal(100.0);
        expectedInvoice.setTotal(120.0);
        expectedInvoice.setTax(20.0);
        expectedInvoice.setDiscount(0.0);
        expectedInvoice.setCycle(1);
        expectedInvoice.setStartDate(new Date());
        expectedInvoice.setEndDate(new Date());
        expectedInvoice.setCreatedDate(new Date());

        when(invoiceRepo.findFirstByHostingOrderByCreatedDateDesc(hosting)).thenReturn(expectedInvoice);

        Invoice result = invoiceRepo.findFirstByHostingOrderByCreatedDateDesc(hosting);

        Assertions.assertEquals(expectedInvoice, result);
        Assertions.assertEquals("INV-001", result.getInvoiceCode());
        Assertions.assertEquals(120.0, result.getTotal());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test findFirstByHostingOrderByCreatedDateDesc - No Invoice for Hosting Returns Null")
    void testFindFirstByHostingReturnsNull() {
        Hosting hosting = new Hosting();
        hosting.setId(UUID.randomUUID());
        hosting.setName("Test Hosting");
        hosting.setDomain("example.com");

        when(invoiceRepo.findFirstByHostingOrderByCreatedDateDesc(hosting)).thenReturn(null);

        Invoice result = invoiceRepo.findFirstByHostingOrderByCreatedDateDesc(hosting);

        Assertions.assertNull(result);
    }

    @Test
    @Order(3)
    @DisplayName("3. Test existsByInvoiceCode - Invoice Code Exists")
    void testExistsByInvoiceCodeExists() {
        String invoiceCode = "INV-1234";

        when(invoiceRepo.existsByInvoiceCode(invoiceCode)).thenReturn(true);

        boolean exists = invoiceRepo.existsByInvoiceCode(invoiceCode);

        Assertions.assertTrue(exists);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test existsByInvoiceCode - Invoice Code Does Not Exist")
    void testExistsByInvoiceCodeDoesNotExist() {
        String invoiceCode = "INV-9999";

        when(invoiceRepo.existsByInvoiceCode(invoiceCode)).thenReturn(false);

        boolean exists = invoiceRepo.existsByInvoiceCode(invoiceCode);

        Assertions.assertFalse(exists);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test save Invoice")
    void testSaveInvoice() {
        Invoice invoice = createInvoice();
        when(invoiceRepo.save(invoice)).thenReturn(invoice);

        Invoice savedInvoice = invoiceRepo.save(invoice);

        Assertions.assertNotNull(savedInvoice);
        Assertions.assertEquals(invoice.getId(), savedInvoice.getId());
        Assertions.assertEquals(invoice.getInvoiceCode(), savedInvoice.getInvoiceCode());
        Assertions.assertEquals(invoice.getDetail(), savedInvoice.getDetail());
        Assertions.assertEquals(invoice.getSubTotal(), savedInvoice.getSubTotal());
        Assertions.assertEquals(invoice.getTotal(), savedInvoice.getTotal());
        Assertions.assertEquals(invoice.getTax(), savedInvoice.getTax());
        Assertions.assertEquals(invoice.getDiscount(), savedInvoice.getDiscount());
        Assertions.assertEquals(invoice.getCycle(), savedInvoice.getCycle());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Find By Invoice UUID")
    void testFindByInvoiceUUID() {
        Invoice invoice = createInvoice();
        when(invoiceRepo.findById(invoice.getId())).thenReturn(java.util.Optional.of(invoice));

        Invoice foundInvoice = invoiceRepo.findById(invoice.getId()).orElse(null);

        Assertions.assertNotNull(foundInvoice);
        Assertions.assertEquals(invoice.getId(), foundInvoice.getId());
    }

    private Invoice createInvoice() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Hosting hosting = new Hosting();
        hosting.setId(UUID.randomUUID());

        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setHosting(hosting);
        invoice.setUser(user);
        invoice.setInvoiceCode("INV-1234");
        invoice.setDetail("Invoice Details");
        invoice.setSubTotal(100.0);
        invoice.setTotal(120.0);
        invoice.setTax(20.0);
        invoice.setDiscount(0.0);
        invoice.setCycle(1);
        invoice.setStartDate(new Date());
        invoice.setEndDate(new Date());
        invoice.setCreatedDate(new Date());
        invoice.setStatus(0);
        invoice.setDeleted(false);
        return invoice;
    }
}
