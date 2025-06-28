package com.alexistdev.geobill.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserDTO {
    private String id;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
