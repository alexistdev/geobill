package com.alexistdev.geobill.services;

import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.HostingRepo;
import com.alexistdev.geobill.request.HostingRequest;
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

    public HostingService(HostingRepo hostingRepo, ProductService productService, UserService userService) {
        this.hostingRepo = hostingRepo;
        this.productService = productService;
        this.userService = userService;
    }

    @Transactional
    public Hosting addHosting(HostingRequest hostingRequest){
        User userFound = userService.findUserByUUID(UUID.fromString(hostingRequest.getUserId()));
        Product productResult = productService.findEntityById(UUID.fromString(hostingRequest.getProductId()));

        Date startDate = new Date();
        Date endDate = this.getEndDate(hostingRequest.getCycle(), startDate);

        Hosting hosting = new Hosting();
        hosting.setName(String.format("%s - %s", productResult.getName(), hostingRequest.getDomainName()));
        hosting.setDomain(hostingRequest.getDomainName());
        hosting.setUser(userFound);
        hosting.setProduct(productResult);
        hosting.setPrice(hostingRequest.getPrice());
        hosting.setStartDate(startDate);
        hosting.setEndDate(endDate);
        hosting.setStatus(0);
        hosting.setCreatedBy(userFound.getUsername());
        hosting.setCreatedDate(new Date());
        hosting.setModifiedBy(userFound.getUsername());
        hosting.setModifiedDate(new Date());
        hosting.setIsDeleted(false);
        return hostingRepo.save(hosting);
    }

    private Date getEndDate(int cycle , Date startDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MONTH, cycle);
        return calendar.getTime();
    }

}
