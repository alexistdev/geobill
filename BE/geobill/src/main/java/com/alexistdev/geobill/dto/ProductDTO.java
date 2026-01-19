package com.alexistdev.geobill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO {
    private String id;
    private String name;
    @JsonIgnoreProperties({ "createdDate", "modifiedDate", "createdBy", "modifiedBy" })
    private ProductTypeDTO productTypeDTO;
    private Double price;
    private Integer cycle;
    private String capacity;
    private String bandwith;
    private String addon_domain;
    private String database_account;
    private String ftp_account;
    private String info1;
    private String info2;
    private String info3;
    private String info4;
    private String info5;
}
