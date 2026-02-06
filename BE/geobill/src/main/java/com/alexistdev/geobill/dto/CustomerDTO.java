package com.alexistdev.geobill.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDTO {
    public String id;
    public String businessName;
    public String address1;
    public String address2;
    public String city;
    public String state;
    public String country;
    public String postCode;
    public String phone;
}
