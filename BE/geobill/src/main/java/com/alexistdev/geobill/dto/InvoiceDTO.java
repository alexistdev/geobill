package com.alexistdev.geobill.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class InvoiceDTO {
    public String id;
    public String invoiceCode;
    public String detail;
    public Double subTotal;
    public Double total;
    public Double tax;
    public Double discount;
    public Date startDate;
    public Date endDate;
    public int status;
    public int cycle;
}
