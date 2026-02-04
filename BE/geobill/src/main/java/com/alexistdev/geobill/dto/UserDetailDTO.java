package com.alexistdev.geobill.dto;

import com.alexistdev.geobill.models.entity.Customer;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class UserDetailDTO {
    private String id;
    private String fullName;
    private String email;
    private String role;
    private Date createdDate;
    private Date modifiedDate;
    private CustomerDTO customer;
}
