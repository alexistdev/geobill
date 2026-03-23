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
    private Double subTotal;
    private Double total;
    private Double tax;
    private Double discount;
    private String startDate;
    private String endDate;
    private int status;
    private int cycle;
}
