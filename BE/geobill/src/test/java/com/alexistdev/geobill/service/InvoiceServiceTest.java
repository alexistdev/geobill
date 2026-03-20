package com.alexistdev.geobill.service;

import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.Invoice;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.InvoiceRepo;
import com.alexistdev.geobill.services.InvoiceService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvoiceServiceTest {

    @Mock
    private InvoiceRepo invoiceRepo;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    @Order(1)
    @DisplayName("1. Test Create Invoice - should map fields and save invoice")
    void createInvoice_shouldMapFieldsAndSave() {
        Hosting hosting = createHosting();
        int cycle = 12;

        when(invoiceRepo.existsByInvoiceCode(any())).thenReturn(false);
        when(invoiceRepo.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice result = invoiceService.createInvoice(hosting, cycle);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(hosting.getUser(), result.getUser());
        Assertions.assertEquals(hosting, result.getHosting());
        Assertions.assertTrue(result.getInvoiceCode().startsWith("INV-"));
        Assertions.assertEquals("Hosting service for " + hosting.getDomain(), result.getDetail());
        Assertions.assertEquals(hosting.getPrice(), result.getSubTotal());
        Assertions.assertEquals(hosting.getPrice(), result.getTotal());
        Assertions.assertEquals(0.0, result.getTax());
        Assertions.assertEquals(0.0, result.getDiscount());
        Assertions.assertEquals(cycle, result.getCycle());
        Assertions.assertEquals(hosting.getStartDate(), result.getStartDate());
        Assertions.assertEquals(hosting.getEndDate(), result.getEndDate());
        Assertions.assertEquals(0, result.getStatus());

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo, times(1)).save(captor.capture());
        Assertions.assertEquals(result.getInvoiceCode(), captor.getValue().getInvoiceCode());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test create Invoice - should retry when generated code already exists")
    void createInvoice_shouldRetryWhenCodeExists() {
        Hosting hosting = createHosting();

        when(invoiceRepo.existsByInvoiceCode(any())).thenReturn(true, false);
        when(invoiceRepo.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice result = invoiceService.createInvoice(hosting, 1);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getInvoiceCode().startsWith("INV-"));
        verify(invoiceRepo, times(2)).existsByInvoiceCode(any());
        verify(invoiceRepo, times(1)).save(any(Invoice.class));
    }

    @Test
    @Order(3)
    @DisplayName("3. Test findLatestInvoiceByHosting - should return repository result")
    void findLatestInvoiceByHosting_shouldReturnRepositoryResult() {
        Hosting hosting = createHosting();
        Invoice latestInvoice = new Invoice();
        latestInvoice.setId(UUID.randomUUID());
        latestInvoice.setHosting(hosting);
        latestInvoice.setInvoiceCode("INV-LATEST");

        when(invoiceRepo.findFirstByHostingOrderByCreatedDateDesc(hosting)).thenReturn(latestInvoice);

        Invoice result = invoiceService.findLatestInvoiceByHosting(hosting);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(latestInvoice.getId(), result.getId());
        Assertions.assertEquals("INV-LATEST", result.getInvoiceCode());
        verify(invoiceRepo, times(1)).findFirstByHostingOrderByCreatedDateDesc(hosting);
    }

    private Hosting createHosting() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("invoice-test@example.com");

        Hosting hosting = new Hosting();
        hosting.setId(UUID.randomUUID());
        hosting.setUser(user);
        hosting.setDomain("example.com");
        hosting.setPrice(150000.0);
        hosting.setStartDate(new Date());
        hosting.setEndDate(new Date());
        return hosting;
    }
}
