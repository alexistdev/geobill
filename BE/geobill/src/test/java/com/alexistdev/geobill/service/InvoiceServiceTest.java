package com.alexistdev.geobill.service;

import com.alexistdev.geobill.dto.InvoiceUserDTO;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.factory.InvoiceFactory;
import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.Invoice;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.InvoiceRepo;
import com.alexistdev.geobill.request.HostingRequest;
import com.alexistdev.geobill.services.InvoiceService;
import com.alexistdev.geobill.services.ProductService;
import com.alexistdev.geobill.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvoiceServiceTest {

    @Mock
    private InvoiceRepo invoiceRepo;

    @Mock
    private MessagesUtils messagesUtils;

    @Mock
    private ProductService productService;

    @Mock
    private InvoiceFactory invoiceFactory;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    @Order(1)
    @DisplayName("1. Test Create Invoice - should map fields and save invoice")
    void createInvoice_shouldMapFieldsAndSave() {
        Hosting hosting = createHosting();
        int cycle = 12;
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(150000.0);
        HostingRequest request = createHostingRequest(product.getId(), cycle);

        Invoice expectedInvoice = createInvoice("INV-001", cycle, product.getPrice() * cycle, product.getPrice() * cycle, hosting.getStartDate(), hosting.getEndDate());
        expectedInvoice.setUser(hosting.getUser());
        expectedInvoice.setHosting(hosting);

        when(invoiceFactory.createInvoice(hosting, request)).thenReturn(expectedInvoice);
        when(invoiceRepo.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice result = invoiceService.createInvoice(hosting, request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedInvoice, result);

        verify(invoiceRepo, times(1)).save(expectedInvoice);
        verify(invoiceFactory, times(1)).createInvoice(hosting, request);
    }

    @Test
    @Order(2)
    @DisplayName("2. Test create Invoice - should retry when generated code already exists")
    void createInvoice_shouldRetryWhenCodeExists() {
        Hosting hosting = createHosting();
        int cycle = 1;
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(150000.0);
        HostingRequest request = createHostingRequest(product.getId(), cycle);

        Invoice expectedInvoice1 = createInvoice("INV-001", cycle, product.getPrice() * cycle, product.getPrice() * cycle, hosting.getStartDate(), hosting.getEndDate());
        Invoice expectedInvoice2 = createInvoice("INV-002", cycle, product.getPrice() * cycle, product.getPrice() * cycle, hosting.getStartDate(), hosting.getEndDate());

        when(invoiceFactory.createInvoice(hosting, request)).thenReturn(expectedInvoice1).thenReturn(expectedInvoice2);
        when(invoiceRepo.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice result = invoiceService.createInvoice(hosting, request);

        Assertions.assertNotNull(result);
        verify(invoiceFactory, times(1)).createInvoice(hosting, request);
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

    @Test
    @Order(4)
    @DisplayName("4. Test getAllInvoicesByUser - should return mapped page")
    void getAllInvoicesByUser_shouldReturnMappedPage() {
        User user = new User();
        user.setId(UUID.randomUUID());
        Pageable pageable = PageRequest.of(0, 10);

        Invoice invoice1 = createInvoice("INV-001", 1, 100000.0, 120000.0, new Date(1735689600000L), new Date(1738368000000L));
        Invoice invoice2 = createInvoice("INV-002", 12, 200000.0, 240000.0, new Date(1740787200000L), new Date(1743465600000L));
        Page<Invoice> invoicePage = new PageImpl<>(Arrays.asList(invoice1, invoice2), pageable, 2);

        when(invoiceRepo.findByUserIdAndIsDeletedFalse(eq(user), eq(pageable))).thenReturn(invoicePage);

        Page<InvoiceUserDTO> result = invoiceService.getAllInvoicesByUser(pageable, user);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals(2, result.getContent().size());

        InvoiceUserDTO first = result.getContent().getFirst();
        Assertions.assertEquals(invoice1.getId().toString(), first.getId());
        Assertions.assertEquals(invoice1.getHosting().getId().toString(), first.getHostingId());
        Assertions.assertEquals("INV-001", first.getInvoiceCode());
        Assertions.assertEquals("Invoice detail for INV-001", first.getDetail());
        Assertions.assertEquals(100000.0, first.getSubTotal());
        Assertions.assertEquals(120000.0, first.getTotal());
        Assertions.assertEquals(10.0, first.getTax());
        Assertions.assertEquals(0.0, first.getDiscount());
        Assertions.assertEquals(1, first.getCycle());
        Assertions.assertEquals(0, first.getStatus());

        verify(invoiceRepo, times(1)).findByUserIdAndIsDeletedFalse(user, pageable);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test getAllInvoicesByUser - should return empty page when no invoices")
    void getAllInvoicesByUser_shouldReturnEmptyPage() {
        User user = new User();
        user.setId(UUID.randomUUID());
        Pageable pageable = PageRequest.of(0, 5);
        Page<Invoice> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(invoiceRepo.findByUserIdAndIsDeletedFalse(eq(user), eq(pageable))).thenReturn(emptyPage);

        Page<InvoiceUserDTO> result = invoiceService.getAllInvoicesByUser(pageable, user);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());
        verify(invoiceRepo, times(1)).findByUserIdAndIsDeletedFalse(user, pageable);
    }

    @Test
    @Order(6)
    @DisplayName("6. Test convertToInvoiceUserDTO - should map all invoice fields correctly")
    void convertToInvoiceUserDTO_shouldMapAllFieldsCorrectly() throws Exception {
        Date startDate = new Date(1735689600000L);
        Date endDate = new Date(1738368000000L);
        Invoice invoice = createInvoice("INV-REFLECTION", 3, 50000.0, 60000.0, startDate, endDate);

        Method method = InvoiceService.class.getDeclaredMethod("convertToInvoiceUserDTO", Invoice.class);
        method.setAccessible(true);
        InvoiceUserDTO result = (InvoiceUserDTO) method.invoke(invoiceService, invoice);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(invoice.getId().toString(), result.getId());
        Assertions.assertEquals(invoice.getHosting().getId().toString(), result.getHostingId());
        Assertions.assertEquals(invoice.getInvoiceCode(), result.getInvoiceCode());
        Assertions.assertEquals(invoice.getDetail(), result.getDetail());
        Assertions.assertEquals(invoice.getSubTotal(), result.getSubTotal());
        Assertions.assertEquals(invoice.getTotal(), result.getTotal());
        Assertions.assertEquals(invoice.getTax(), result.getTax());
        Assertions.assertEquals(invoice.getDiscount(), result.getDiscount());
        Assertions.assertEquals(invoice.getCycle(), result.getCycle());
        Assertions.assertEquals(dateFormat.format(startDate), result.getStartDate());
        Assertions.assertEquals(dateFormat.format(endDate), result.getEndDate());
        Assertions.assertEquals(invoice.getStatus(), result.getStatus());
    }


    @Test
    @Order(7)
    @DisplayName("7. Test getInvoiceById - should return InvoiceUserDTO when invoice exists")
    void getInvoiceById_shouldReturnInvoiceUserDTOWhenInvoiceExists() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = createInvoice("INV-GET-BY-ID", 6, 70000.0, 80000.0, new Date(1743552000000L), new Date(1746230400000L));
        invoice.setId(invoiceId);

        when(invoiceRepo.findById(invoiceId)).thenReturn(Optional.of(invoice));

        InvoiceUserDTO result = invoiceService.getInvoiceById(invoiceId.toString());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(invoiceId.toString(), result.getId());
        Assertions.assertEquals(invoice.getInvoiceCode(), result.getInvoiceCode());
    }

    @Test
    @Order(8)
    @DisplayName("8. Test getInvoiceById - should throw NotFoundException when invoice does not exist")
    void getInvoiceById_shouldThrowNotFoundExceptionWhenInvoiceDoesNotExist() {
        UUID invoiceId = UUID.randomUUID();
        when(invoiceRepo.findById(invoiceId)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage("invoiceservice.invoice_not_found", invoiceId.toString()))
                .thenReturn("Invoice not found");

        Assertions.assertThrows(NotFoundException.class, () ->
            invoiceService.getInvoiceById(invoiceId.toString()));

        verify(invoiceRepo, times(1)).findById(invoiceId);
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

    private Invoice createInvoice(String invoiceCode, int cycle, double subTotal, double total, Date startDate, Date endDate) {
        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setUser(new User());
        Hosting hosting = new Hosting();
        hosting.setId(UUID.randomUUID());
        invoice.setHosting(hosting);
        invoice.setInvoiceCode(invoiceCode);
        invoice.setDetail("Invoice detail for " + invoiceCode);
        invoice.setSubTotal(subTotal);
        invoice.setTotal(total);
        invoice.setTax(10.0);
        invoice.setDiscount(0.0);
        invoice.setCycle(cycle);
        invoice.setStartDate(startDate);
        invoice.setEndDate(endDate);
        invoice.setStatus(0);
        return invoice;
    }

    private HostingRequest createHostingRequest(UUID productId, int cycle) {
        HostingRequest request = new HostingRequest();
        request.setProductId(productId.toString());
        request.setCycle(cycle);
        return request;
    }
}
