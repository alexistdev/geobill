package com.alexistdev.geobill.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserDTO {
    private String id;
    private String fullName;
    private String email;
    private String role;
    private List<MenuDTO> menus;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String homeURL;
}
