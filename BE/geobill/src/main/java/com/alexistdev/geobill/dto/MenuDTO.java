package com.alexistdev.geobill.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MenuDTO {
    private String id;
    private String name;
    private String urlink;
    private String classlink;
    private String sortOrder;
    private String icon;
    private int typeMenu;
    private UUID parentId;
}
