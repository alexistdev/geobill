package com.alexistdev.geobill.service;

import com.alexistdev.geobill.dto.HostingDTO;
import com.alexistdev.geobill.dto.HostingUserDTO;
import com.alexistdev.geobill.exceptions.ConflictException;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.*;
import com.alexistdev.geobill.models.repository.HostingRepo;
import com.alexistdev.geobill.request.HostingRequest;
import com.alexistdev.geobill.services.*;
import com.alexistdev.geobill.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HostingServiceTest {

    @Mock
    private HostingRepo hostingRepo;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private CustomerService customerService;

    @Mock
    private MessagesUtils messagesUtils;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private HostingService hostingService;

    private HostingRequest hostingRequest;
    private User user;
    private Hosting hosting;
    private Product product;
    private Invoice invoice;
    private Customer customer;

    @BeforeEach
    void setUp() {
        String userId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        String domainName = "example.com";
        Double price = 100.0;
        int cycle = 1;
        Date startDate = new Date();
        Date endDate = this.getEndDate(cycle, startDate);

        hostingRequest = new HostingRequest();
        hostingRequest.setUserId(userId);
        hostingRequest.setProductId(productId);
        hostingRequest.setDomainName(domainName);
        hostingRequest.setPrice(price);
        hostingRequest.setCycle(cycle);

        user = new User();
        user.setId(UUID.fromString(userId));
        user.setEmail("test@gmail.com");
        user.setPassword("password");
        user.setFullName("Test User");

        product = new Product();
        product.setId(UUID.fromString(productId));
        product.setName("Hosting Product");


        hosting = new Hosting();
        hosting.setId(UUID.randomUUID());
        hosting.setUser(user);
        hosting.setProduct(product);
        hosting.setName("Hosting Product - " + domainName);
        hosting.setHostingCode("GE-12345678");
        hosting.setDomain(domainName);
        hosting.setPrice(price);
        hosting.setStartDate(startDate);
        hosting.setEndDate(endDate);
        hosting.setStatus(0);
        hosting.setCreatedBy("system");
        hosting.setCreatedDate(new Date());
        hosting.setModifiedBy("system");
        hosting.setModifiedDate(new Date());
        hosting.setIsDeleted(false);

        invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setCycle(cycle);

        customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setBusinessName("Test Business");
    }

    private Date getEndDate(int cycle, Date startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MONTH, cycle);
        return calendar.getTime();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Add Hosting")
    void testAddHosting() {
        when(userService.findUserByUUID(any(UUID.class))).thenReturn(user);
        when(productService.findEntityById(any(UUID.class))).thenReturn(product);
        when(hostingRepo.existsByHostingCode(anyString())).thenReturn(false);
        when(hostingRepo.existsByUser_IdAndStatus(any(UUID.class), eq(0))).thenReturn(false);
        when(hostingRepo.save(any(Hosting.class))).thenReturn(hosting);
        when(invoiceService.createInvoice(any(Hosting.class),anyInt())).thenReturn(invoice);
        when(customerService.findCustomerByUserId(any(User.class))).thenReturn(customer);

        HostingDTO result = hostingService.addHosting(hostingRequest);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(hosting.getDomain(), result.getDomainName());
        Assertions.assertEquals(hosting.getPrice(), result.getPrice());
        Assertions.assertEquals(invoice.getCycle(), result.getCycle());
        Assertions.assertNotNull(result.getCustomerDTO());
        Assertions.assertEquals(customer.getBusinessName(), result.getCustomerDTO().getBusinessName());

        verify(hostingRepo, times(1)).save(any(Hosting.class));
        verify(invoiceService, times(1)).createInvoice(any(Hosting.class),anyInt());
        verify(customerService, times(1)).findCustomerByUserId(any(User.class));
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Add Hosting - User Not Found")
    void testAddHosting_UserNotFound() {
        UUID nonExistingUserId = UUID.randomUUID();
        hostingRequest.setUserId(nonExistingUserId.toString());

        when(userService.findUserByUUID(any(UUID.class))).thenReturn(null);
        when(messagesUtils.getMessage("hostingservice.user_not_found")).thenReturn("User not found");

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> hostingService.addHosting(hostingRequest));
        Assertions.assertEquals("User not found", exception.getMessage());
        verify(userService).findUserByUUID(UUID.fromString(hostingRequest.getUserId()));
        verifyNoInteractions(productService, hostingRepo, invoiceService, customerService);
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Add Hosting - Product Not Found")
    void testAddHosting_ProductNotFound() {
        when(userService.findUserByUUID(any(UUID.class))).thenReturn(user);
        when(productService.findEntityById(any(UUID.class))).thenReturn(null);
        when(messagesUtils.getMessage("hostingservice.product_not_found")).thenReturn("Product not found");

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> hostingService.addHosting(hostingRequest) );
        Assertions.assertEquals("Product not found", exception.getMessage());
        verify(userService).findUserByUUID(UUID.fromString(hostingRequest.getUserId()));
        verify(productService).findEntityById(UUID.fromString(hostingRequest.getProductId()));
        verifyNoInteractions(hostingRepo, invoiceService, customerService);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Add Hosting - User Already Has Pending Hosting")
    void testAddHosting_UserAlreadyHasPendingHosting() {
        when(userService.findUserByUUID(any(UUID.class))).thenReturn(user);
        when(productService.findEntityById(any(UUID.class))).thenReturn(product);
        when(hostingRepo.existsByUser_IdAndStatus(any(UUID.class), eq(0))).thenReturn(true);
        when(messagesUtils.getMessage("hostingservice.user_already_have_pending_hosting"))
                .thenReturn("User already has pending hosting");

        ConflictException exception = Assertions.assertThrows(ConflictException.class,
                () -> hostingService.addHosting(hostingRequest));
        Assertions.assertEquals("User already has pending hosting", exception.getMessage());

        verify(userService).findUserByUUID(UUID.fromString(hostingRequest.getUserId()));
        verify(productService).findEntityById(UUID.fromString(hostingRequest.getProductId()));
        verify(hostingRepo).existsByUser_IdAndStatus(user.getId(), 0);
        verifyNoInteractions(invoiceService, customerService);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Add Hosting - Hosting Code Collision Retry")
    void testAddHosting_HostingCodeCollision() {
        when(userService.findUserByUUID(any(UUID.class))).thenReturn(user);
        when(productService.findEntityById(any(UUID.class))).thenReturn(product);
        //collision
        when(hostingRepo.existsByHostingCode(anyString())).thenReturn(true, false);
        when(hostingRepo.existsByUser_IdAndStatus(any(UUID.class), eq(0))).thenReturn(false);
        when(hostingRepo.save(any(Hosting.class))).thenReturn(hosting);
        when(invoiceService.createInvoice(any(Hosting.class),anyInt())).thenReturn(invoice);
        when(customerService.findCustomerByUserId(any(User.class))).thenReturn(customer);

        HostingDTO result = hostingService.addHosting(hostingRequest);

        Assertions.assertNotNull(result);
        // existsByHostingCode should have been called twice (1 collision + 1 success)
        verify(hostingRepo, times(2)).existsByHostingCode(anyString());
        verify(hostingRepo, times(1)).save(any(Hosting.class));
        verify(invoiceService, times(1)).createInvoice(any(Hosting.class),anyInt());
        verify(customerService, times(1)).findCustomerByUserId(any(User.class));
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Add Hosting - Invalid User UUID")
    void testAddHosting_InvalidUserUUID() {
        hostingRequest.setUserId("not-a-valid-uuid");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> hostingService.addHosting(hostingRequest));

        verifyNoInteractions(productService, hostingRepo, invoiceService, customerService);
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Add Hosting - Invalid Product UUID")
    void testAddHosting_InvalidProductUUID() {
        when(userService.findUserByUUID(any(UUID.class))).thenReturn(user);
        hostingRequest.setProductId("not-a-valid-uuid");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> hostingService.addHosting(hostingRequest));

        verify(userService).findUserByUUID(UUID.fromString(hostingRequest.getUserId()));
        verifyNoInteractions(productService, hostingRepo, invoiceService, customerService);
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Add Hosting - Verify Saved Entity Fields")
    void testAddHosting_VerifySavedEntity() {
        int cycleTest = 1;
        when(userService.findUserByUUID(any(UUID.class))).thenReturn(user);
        when(productService.findEntityById(any(UUID.class))).thenReturn(product);
        when(hostingRepo.existsByHostingCode(anyString())).thenReturn(false);
        when(hostingRepo.existsByUser_IdAndStatus(any(UUID.class), eq(0))).thenReturn(false);
        when(hostingRepo.save(any(Hosting.class))).thenReturn(hosting);
        when(invoiceService.createInvoice(any(Hosting.class),anyInt())).thenReturn(invoice);
        when(customerService.findCustomerByUserId(any(User.class))).thenReturn(customer);

        hostingService.addHosting(hostingRequest);

        ArgumentCaptor<Hosting> captor = ArgumentCaptor.forClass(Hosting.class);
        verify(hostingRepo).save(captor.capture());

        Hosting savedHosting = captor.getValue();
        Assertions.assertEquals("example.com", savedHosting.getDomain());
        Assertions.assertEquals(100.0, savedHosting.getPrice());
        Assertions.assertEquals(user, savedHosting.getUser());
        Assertions.assertEquals(product, savedHosting.getProduct());
        Assertions.assertEquals(0, savedHosting.getStatus());
        Assertions.assertFalse(savedHosting.getIsDeleted());
        Assertions.assertEquals(user.getEmail(), savedHosting.getCreatedBy());
        Assertions.assertEquals("Hosting Product - example.com", savedHosting.getName());
        Assertions.assertTrue(savedHosting.getHostingCode().startsWith("GE-"));
        Assertions.assertNotNull(savedHosting.getStartDate());
        Assertions.assertNotNull(savedHosting.getEndDate());

        verify(invoiceService).createInvoice(hosting,cycleTest);
        verify(customerService).findCustomerByUserId(user);
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Get All Hostings By User")
    void testGetAllHostingByUser() {
        Pageable pageable = Pageable.ofSize(10);
        List<Hosting> hostings = Collections.singletonList(hosting);
        Page<Hosting> hostingPage = new PageImpl<>(hostings, pageable, hostings.size());
        when(hostingRepo.findByUserAndIsDeletedFalse(any(), eq(user))).thenReturn(hostingPage);
        Invoice latestInvoice = new Invoice();
        latestInvoice.setId(UUID.randomUUID());
        when(invoiceService.findLatestInvoiceByHosting(any(Hosting.class))).thenReturn(latestInvoice);

        Page<HostingUserDTO> result = hostingService.getAllHostingsByUser(Pageable.ofSize(10), user);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getContent().size());

        HostingUserDTO dto = result.getContent().getFirst();
        Assertions.assertEquals(hosting.getId().toString(), dto.getId());
        Assertions.assertEquals(hosting.getName(), dto.getName());
        Assertions.assertEquals(hosting.getDomain(), dto.getDomain());
        Assertions.assertEquals(hosting.getPrice(), dto.getPrice());
        Assertions.assertEquals(latestInvoice.getId().toString(), dto.getInvoiceId());

        verify(hostingRepo, times(1)).findByUserAndIsDeletedFalse(any(), eq(user));
        verify(invoiceService, times(1)).findLatestInvoiceByHosting(any(Hosting.class));
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Get All Hostings By User - Empty Result")
    void testGetAllHostings_EmptyResult() {
        Pageable pageable = Pageable.ofSize(10);
        Page<Hosting> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(hostingRepo.findByUserAndIsDeletedFalse(pageable, user)).thenReturn(emptyPage);

        Page<HostingUserDTO> result = hostingService.getAllHostingsByUser(Pageable.ofSize(10), user);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getContent().isEmpty());

        verify(hostingRepo, times(1)).findByUserAndIsDeletedFalse(any(), eq(user));
        verify(invoiceService, never()).findLatestInvoiceByHosting(any(Hosting.class));
    }

    @Test
    @Order(11)
    @DisplayName("11. Test Get All Hostings By Filter")
    void testGetAllHostingsByFilter() {
        Pageable pageable = Pageable.ofSize(10);
        String keyword = "example";
        List<Hosting> hostings = Collections.singletonList(hosting);
        Page<Hosting> hostingPage = new PageImpl<>(hostings, pageable, hostings.size());

        when(hostingRepo.findByUserWithFilterAndIsDeletedFalse(keyword, user, pageable)).thenReturn(hostingPage);

        Page<Hosting> result = hostingService.getAllHostingsByFilter(pageable, keyword, user);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getContent().size());
        Assertions.assertEquals(hosting, result.getContent().getFirst());

        verify(hostingRepo, times(1)).findByUserWithFilterAndIsDeletedFalse(keyword, user, pageable);
    }

    @Test
    @Order(12)
    @DisplayName("12. Test Get All Hostings By Filter - Empty Result")
    void testGetAllHostingsByFilter_EmptyResult() {
        Pageable pageable = Pageable.ofSize(10);
        String keyword = "nonexistent";
        Page<Hosting> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(hostingRepo.findByUserWithFilterAndIsDeletedFalse(keyword, user, pageable)).thenReturn(emptyPage);

        Page<Hosting> result = hostingService.getAllHostingsByFilter(pageable, keyword, user);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getContent().isEmpty());

        verify(hostingRepo, times(1)).findByUserWithFilterAndIsDeletedFalse(keyword, user, pageable);
    }
}
