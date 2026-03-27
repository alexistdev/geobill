package com.alexistdev.geobill.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceUserDTO {
    private String id;
    private String hostingId;
    private String invoiceCode;
    private String detail;
    private double price;
    private double subTotal;
    private double total;
    private double tax;
    private double discount;
    private String startDate;
    private String endDate;
    private int status;
    private int cycle;
}
