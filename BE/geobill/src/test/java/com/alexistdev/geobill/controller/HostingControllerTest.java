package com.alexistdev.geobill.controller;

import com.alexistdev.geobill.controllers.HostingController;
import com.alexistdev.geobill.dto.HostingDTO;
import com.alexistdev.geobill.dto.InvoiceDTO;
import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.request.HostingRequest;
import com.alexistdev.geobill.services.HostingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;


import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HostingControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private StubHostingService hostingService;
    private HostingController hostingController;
    private MockMvc mockMvc;

    private UUID hostingId;
    private UUID userId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        hostingService = new StubHostingService();
        hostingController = new HostingController(hostingService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(hostingController)
                .setValidator(validator)
                .build();

        hostingId = UUID.randomUUID();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    private HostingRequest createHostingRequest() {
        HostingRequest request = new HostingRequest();
        request.setUserId(userId.toString());
        request.setProductId(productId.toString());
        request.setDomainName("example.com");
        request.setPrice(99.99);
        request.setCycle(12);
        return request;
    }

    private HostingDTO createHostingDTO(UUID ownerId, UUID selectedProductId) {
        HostingDTO dto = new HostingDTO();
        dto.setId(hostingId);
        dto.setUserId(ownerId);
        dto.setProductId(selectedProductId);
        dto.setDomainName("example.com");
        dto.setPrice(99.99);
        dto.setCycle(24);

        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setId(UUID.randomUUID().toString());
        invoiceDTO.setInvoiceCode("INV-2024-001");
        invoiceDTO.setDetail("Hosting invoice");
        invoiceDTO.setSubTotal(99.99);
        invoiceDTO.setTotal(119.99);
        invoiceDTO.setTax(20.00);
        invoiceDTO.setDiscount(0.0);
        invoiceDTO.setStartDate(new Date());
        invoiceDTO.setEndDate(new Date());
        invoiceDTO.setStatus(1);
        invoiceDTO.setCycle(12);

        dto.setInvoiceDTO(invoiceDTO);

        return dto;
    }

    @Test
    @Order(1)
    @DisplayName("1. addHosting should return CREATED with mapped payload")
    void testAddHostingSuccess() {
        HostingRequest request = createHostingRequest();
        hostingService.response = createHostingDTO(userId, productId);

        ResponseEntity<ResponseData<HostingDTO>> response = hostingController.addHosting(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ResponseData<HostingDTO> body = response.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertTrue(body.isStatus());
        Assertions.assertEquals(1, body.getMessages().size());
        Assertions.assertEquals("Hosting added successfully", body.getMessages().getFirst());

        HostingDTO payload = body.getPayload();
        Assertions.assertNotNull(payload);
        Assertions.assertEquals(hostingId, payload.getId());
        Assertions.assertEquals(userId, payload.getUserId());
        Assertions.assertEquals(productId, payload.getProductId());
        Assertions.assertEquals("example.com", payload.getDomainName());
        Assertions.assertEquals(99.99, payload.getPrice());
        Assertions.assertEquals(24, payload.getCycle());
        Assertions.assertNotNull(payload.getInvoiceDTO());

        Assertions.assertEquals(1, hostingService.callCount);
        Assertions.assertSame(request, hostingService.lastRequest);
    }

    @Test
    @Order(2)
    @DisplayName("2. addHosting should propagate service exception")
    void testAddHostingPropagatesServiceException() {
        HostingRequest request = createHostingRequest();
        hostingService.exceptionToThrow = new RuntimeException("Unexpected error");

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
                () -> hostingController.addHosting(request));

        Assertions.assertEquals("Unexpected error", thrown.getMessage());
        Assertions.assertEquals(1, hostingService.callCount);
        Assertions.assertSame(request, hostingService.lastRequest);
    }

    @Test
    @Order(3)
    @DisplayName("3. POST /api/v1/hosting should return 201 when request is valid")
    void testAddHostingEndpointSuccess() throws Exception {
        HostingRequest request = createHostingRequest();
        hostingService.response = createHostingDTO(userId, productId);

        mockMvc.perform(post("/api/v1/hosting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value("Hosting added successfully"))
                .andExpect(jsonPath("$.payload.id").value(hostingId.toString()))
                .andExpect(jsonPath("$.payload.userId").value(userId.toString()))
                .andExpect(jsonPath("$.payload.productId").value(productId.toString()))
                .andExpect(jsonPath("$.payload.domainName").value("example.com"))
                .andExpect(jsonPath("$.payload.price").value(99.99))
                .andExpect(jsonPath("$.payload.cycle").value(24))
                .andExpect(jsonPath("$.payload.invoiceDTO").exists());

        Assertions.assertEquals(1, hostingService.callCount);
    }

    @Test
    @Order(4)
    @DisplayName("4. POST /api/v1/hosting should return 400 when domainName is missing")
    void testAddHostingEndpointValidationError() throws Exception {
        HostingRequest invalidRequest = createHostingRequest();
        invalidRequest.setDomainName("");

        mockMvc.perform(post("/api/v1/hosting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        Assertions.assertEquals(0, hostingService.callCount);
    }

    @Test
    @Order(5)
    @DisplayName("5. POST /api/v1/hosting should propagate exception when service throws")
    void testAddHostingEndpointServiceException() {
        HostingRequest request = createHostingRequest();
        hostingService.exceptionToThrow = new RuntimeException("Unexpected error");

        Assertions.assertThrows(Exception.class, () ->
                mockMvc.perform(post("/api/v1/hosting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
        );

        Assertions.assertEquals(1, hostingService.callCount);
    }

    private static class StubHostingService extends HostingService {
        private HostingDTO response;
        private RuntimeException exceptionToThrow;
        private int callCount;
        private HostingRequest lastRequest;

        private StubHostingService() {
            super(null, null, null, null,null,null);
        }

        @Override
        public HostingDTO addHosting(HostingRequest hostingRequest) {
            callCount++;
            lastRequest = hostingRequest;
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            return response;
        }
    }
}
