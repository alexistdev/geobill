package com.alexistdev.geobill.services;

import com.alexistdev.geobill.dto.CustomerDTO;
import com.alexistdev.geobill.dto.HostingDTO;
import com.alexistdev.geobill.dto.HostingUserDTO;
import com.alexistdev.geobill.dto.InvoiceDTO;
import com.alexistdev.geobill.exceptions.ConflictException;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.*;
import com.alexistdev.geobill.models.repository.HostingRepo;
import com.alexistdev.geobill.request.HostingRequest;
import com.alexistdev.geobill.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HostingService {

    private final HostingRepo hostingRepo;
    private final ProductService productService;
    private final UserService userService;
    private final MessagesUtils messagesUtils;
    private final InvoiceService invoiceService;
    private final CustomerService customerService;

    public HostingService(HostingRepo hostingRepo, ProductService productService,
                          UserService userService,
                          MessagesUtils messagesUtils,
                          InvoiceService invoiceService,
                          CustomerService customerService) {
        this.hostingRepo = hostingRepo;
        this.productService = productService;
        this.userService = userService;
        this.messagesUtils = messagesUtils;
        this.invoiceService = invoiceService;
        this.customerService = customerService;
    }

    public Page<HostingUserDTO> getAllHostingsByUser(Pageable pageable, User user){
        Page<Hosting> result = hostingRepo.findByUserAndIsDeletedFalse(pageable, user);

        List<HostingUserDTO> hostingUserDTOList =  result.getContent().stream()
                .map(this::convertToHostingUserDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(hostingUserDTOList, pageable, result.getTotalElements());
    }

    private HostingUserDTO convertToHostingUserDTO(Hosting hosting){
        HostingUserDTO hostingUserDTO = new HostingUserDTO();
        Invoice invoice = invoiceService.findLatestInvoiceByHosting(hosting);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        if(invoice != null){
            hostingUserDTO.setInvoiceId(invoice.getId().toString());
        }

        Date endDate = hosting.getEndDate();

        hostingUserDTO.setProductId(hosting.getProduct().getId().toString());
        hostingUserDTO.setId(hosting.getId().toString());
        hostingUserDTO.setName(hosting.getName());
        hostingUserDTO.setDomain(hosting.getDomain());
        hostingUserDTO.setPrice(hosting.getPrice());
        hostingUserDTO.setEndDate(dateFormat.format(endDate));
        hostingUserDTO.setStatus(hosting.getStatus());
        return hostingUserDTO;
    }

    public Page<Hosting> getAllHostingsByFilter(Pageable pageable, String keyword, User user) {
        return hostingRepo.findByUserWithFilterAndIsDeletedFalse(keyword, user, pageable);
    }

    @Transactional
    public HostingDTO addHosting(HostingRequest hostingRequest) {
        User userFound = userService.findUserByUUID(UUID.fromString(hostingRequest.getUserId()));
        if(userFound == null ){
            String userNotFoundMessage= messagesUtils.getMessage("hostingservice.user_not_found");
            throw new NotFoundException(userNotFoundMessage);
        }
        Product productResult = productService.findEntityById(UUID.fromString(hostingRequest.getProductId()));
        if(productResult == null){
            String productNotFoundMessage = messagesUtils.getMessage("hostingservice.product_not_found");
            throw new NotFoundException(productNotFoundMessage);
        }
        if(this.doesUserHaveHostingWithStatus(userFound.getId())){
            String userAlreadyHavePendingHosting = messagesUtils.getMessage("hostingservice.user_already_have_pending_hosting");
            throw new ConflictException(userAlreadyHavePendingHosting);
        }

        Hosting savedHosting = hostingRepo.save(this.createHosting(hostingRequest, userFound, productResult));
        Invoice savedInvoice = invoiceService.createInvoice(savedHosting,hostingRequest.getCycle());
        InvoiceDTO invoiceDTO = createInvoiceDTO(savedInvoice);
        CustomerDTO customerDTO = findCustomer(userFound);
        return createHostingDTO(savedHosting, invoiceDTO,customerDTO);
    }

    private CustomerDTO findCustomer(User user) {
        Customer customerFound = customerService.findCustomerByUserId(user);
        return this.createCustomerDTO(customerFound);
    }

    private CustomerDTO createCustomerDTO(Customer customer) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customer.getId().toString());
        customerDTO.setBusinessName(customer.getBusinessName());
        customerDTO.setAddress1(customer.getAddress1());
        customerDTO.setAddress2(customer.getAddress2());
        customerDTO.setCity(customer.getCity());
        customerDTO.setCountry(customer.getCountry());
        customerDTO.setState(customer.getState());
        customerDTO.setPostCode(customer.getPostCode());
        customerDTO.setPhone(customer.getPhone());
        customerDTO.setCustomerNumber(String.valueOf(customer.getCustomerNumber()));
        return customerDTO;
    }

    private InvoiceDTO createInvoiceDTO(Invoice invoice) {
        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setId(invoice.getId().toString());
        invoiceDTO.setInvoiceCode(invoice.getInvoiceCode());
        invoiceDTO.setDetail(invoice.getDetail());
        invoiceDTO.setSubTotal(invoice.getSubTotal());
        invoiceDTO.setTotal(invoice.getTotal());
        invoiceDTO.setTax(invoice.getTax());
        invoiceDTO.setCycle(invoice.getCycle());
        invoiceDTO.setDiscount(invoice.getDiscount());
        invoiceDTO.setStartDate(invoice.getStartDate());
        invoiceDTO.setEndDate(invoice.getEndDate());
        invoiceDTO.setStatus(invoice.getStatus());
        return invoiceDTO;
    }

    private HostingDTO createHostingDTO(Hosting hosting, InvoiceDTO invoiceDTO, CustomerDTO customerDTO) {
        HostingDTO hostingDTO = new HostingDTO();
        hostingDTO.setId(hosting.getId());
        hostingDTO.setUserId(hosting.getUser().getId());
        hostingDTO.setProductId(hosting.getProduct().getId());
        hostingDTO.setDomainName(hosting.getDomain());
        hostingDTO.setPrice(hosting.getPrice());
        hostingDTO.setCycle(invoiceDTO.getCycle());
        hostingDTO.setInvoiceDTO(invoiceDTO);
        hostingDTO.setCustomerDTO(customerDTO);
        return hostingDTO;
    }

    private Hosting createHosting(HostingRequest hostingRequest, User userFound, Product productResult) {
        Hosting hosting = new Hosting();

        Date startDate = new Date();
        Date endDate = this.getEndDate(hostingRequest.getCycle(), startDate);

        hosting.setHostingCode(generateHostingCode());
        hosting.setName(String.format("%s - %s", productResult.getName(), hostingRequest.getDomainName()));
        hosting.setDomain(hostingRequest.getDomainName());
        hosting.setUser(userFound);
        hosting.setProduct(productResult);
        hosting.setPrice(hostingRequest.getPrice());
        hosting.setStartDate(startDate);
        hosting.setEndDate(endDate);
        hosting.setStatus(0);
        hosting.setCreatedBy(userFound.getEmail());
        hosting.setCreatedDate(new Date());
        hosting.setModifiedBy(userFound.getEmail());
        hosting.setModifiedDate(new Date());
        hosting.setIsDeleted(false);
        return hosting;
    }

    private boolean doesUserHaveHostingWithStatus(UUID userId) {
        return hostingRepo.existsByUser_IdAndStatus(userId, 0);
    }

    private Date getEndDate(int cycle, Date startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MONTH, cycle);
        return calendar.getTime();
    }

    private String generateHostingCode() {
        String code;
        do {
            String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            code = "GE-" + randomStr;
        } while (hostingRepo.existsByHostingCode(code));
        return code;
    }

}
