package com.alexistdev.geobill.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HostingUserDTO {

    public String id;
    public String productId;
    public String invoiceId;
    public String name;
    public String domain;
    public Double price;
    public String endDate;
    public int status;

}
