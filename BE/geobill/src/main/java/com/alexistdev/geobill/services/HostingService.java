package com.alexistdev.geobill.services;

import com.alexistdev.geobill.exceptions.ConflictException;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.HostingRepo;
import com.alexistdev.geobill.request.HostingRequest;
import com.alexistdev.geobill.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class HostingService {

    private final HostingRepo hostingRepo;
    private final ProductService productService;
    private final UserService userService;
    private final MessagesUtils messagesUtils;
    private final InvoiceService invoiceService;

    public HostingService(HostingRepo hostingRepo, ProductService productService, UserService userService, MessagesUtils messagesUtils, InvoiceService invoiceService) {
        this.hostingRepo = hostingRepo;
        this.productService = productService;
        this.userService = userService;
        this.messagesUtils = messagesUtils;
        this.invoiceService = invoiceService;
    }

    @Transactional
    public Hosting addHosting(HostingRequest hostingRequest) {
        User userFound = userService.findUserByUUID(UUID.fromString(hostingRequest.getUserId()));
        Product productResult = productService.findEntityById(UUID.fromString(hostingRequest.getProductId()));
        if(userFound == null ){
            String userNotFoundMessage= messagesUtils.getMessage("hostingservice.user_not_found");
            throw new NotFoundException(userNotFoundMessage);
        }
        if(productResult == null){
            String productNotFoundMessage = messagesUtils.getMessage("hostingservice.product_not_found");
            throw new NotFoundException(productNotFoundMessage);
        }
        if(this.doesUserHaveHostingWithStatus(userFound.getId(), 0)){
            String userAlreadyHavePendingHosting = messagesUtils.getMessage("hostingservice.user_already_have_pending_hosting");
            throw new ConflictException(userAlreadyHavePendingHosting);
        }
        Hosting savedHosting = hostingRepo.save(this.createHosting(hostingRequest, userFound, productResult));
        invoiceService.createInvoice(savedHosting);
        return savedHosting;
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

    private boolean doesUserHaveHostingWithStatus(UUID userId, int status) {
        return hostingRepo.existsByUser_IdAndStatus(userId, status);
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
