package com.alexistdev.geobill.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class HostingDTO {
    private UUID id;
    private UUID userId;
    private UUID productId;
    private UUID invoiceId;
    private String domainName;
    private Double price;
    private Integer cycle;
}
