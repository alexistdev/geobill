package com.alexistdev.geobill.service;

import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.HostingRepo;
import com.alexistdev.geobill.request.HostingRequest;
import com.alexistdev.geobill.services.HostingService;
import com.alexistdev.geobill.services.ProductService;
import com.alexistdev.geobill.services.UserService;
import com.alexistdev.geobill.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import java.util.*;

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
    private MessagesUtils messagesUtils;

    @InjectMocks
    private HostingService hostingService;

    private HostingRequest hostingRequest;
    private User user;
    private Hosting hosting;
    private Product product;

    @BeforeEach
    void setUp() {
        String userId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        String domainName = "example.com";
        Double price = 100.0;
        Integer cycle = 1;
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
        hosting.setUser(user);
        hosting.setProduct(product);
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

        Hosting result = hostingService.addHosting(hostingRequest);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(hosting.getDomain(), result.getDomain());
        Assertions.assertEquals(hosting.getPrice(), result.getPrice());
        Assertions.assertEquals(hosting.getStartDate(), result.getStartDate());
        Assertions.assertEquals(hosting.getEndDate(), result.getEndDate());

        verify(hostingRepo, times(1)).save(any(Hosting.class));
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Add Hosting - User Not Found")
    void testAddHosting_UserNotFound() {
        when(userService.findUserByUUID(any(UUID.class))).thenReturn(null);
        when(productService.findEntityById(any(UUID.class))).thenReturn(product);
        when(messagesUtils.getMessage("hostingservice.user_not_found")).thenReturn("User not found");

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> {
            hostingService.addHosting(hostingRequest);
        });
        Assertions.assertEquals("User not found", exception.getMessage());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Add Hosting - Product Not Found")
    void testAddHosting_ProductNotFound() {
        when(userService.findUserByUUID(any(UUID.class))).thenReturn(user);
        when(productService.findEntityById(any(UUID.class))).thenReturn(null);
        when(messagesUtils.getMessage("hostingservice.product_not_found")).thenReturn("Product not found");

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> {
            hostingService.addHosting(hostingRequest);
        });
        Assertions.assertEquals("Product not found", exception.getMessage());
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

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            hostingService.addHosting(hostingRequest);
        });
        Assertions.assertEquals("User already has pending hosting", exception.getMessage());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Add Hosting - Hosting Code Collision Retry")
    void testAddHosting_HostingCodeCollision() {
        when(userService.findUserByUUID(any(UUID.class))).thenReturn(user);
        when(productService.findEntityById(any(UUID.class))).thenReturn(product);
        // First call returns true (collision), second returns false (unique)
        // this method will generate random code , if already exists in database, it loops again and generate new one
        // this unit test to ensure this to replicate in production, when a collision can be happen.
        when(hostingRepo.existsByHostingCode(anyString())).thenReturn(true, false);
        when(hostingRepo.existsByUser_IdAndStatus(any(UUID.class), eq(0))).thenReturn(false);
        when(hostingRepo.save(any(Hosting.class))).thenReturn(hosting);

        Hosting result = hostingService.addHosting(hostingRequest);

        Assertions.assertNotNull(result);
        // existsByHostingCode should have been called twice (1 collision + 1 success)
        verify(hostingRepo, times(2)).existsByHostingCode(anyString());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Add Hosting - Invalid User UUID")
    void testAddHosting_InvalidUserUUID() {
        hostingRequest.setUserId("not-a-valid-uuid");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            hostingService.addHosting(hostingRequest);
        });
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Add Hosting - Invalid Product UUID")
    void testAddHosting_InvalidProductUUID() {
        hostingRequest.setProductId("not-a-valid-uuid");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            hostingService.addHosting(hostingRequest);
        });
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Add Hosting - Verify Saved Entity Fields")
    void testAddHosting_VerifySavedEntity() {
        when(userService.findUserByUUID(any(UUID.class))).thenReturn(user);
        when(productService.findEntityById(any(UUID.class))).thenReturn(product);
        when(hostingRepo.existsByHostingCode(anyString())).thenReturn(false);
        when(hostingRepo.existsByUser_IdAndStatus(any(UUID.class), eq(0))).thenReturn(false);
        when(hostingRepo.save(any(Hosting.class))).thenReturn(hosting);

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
    }
}
