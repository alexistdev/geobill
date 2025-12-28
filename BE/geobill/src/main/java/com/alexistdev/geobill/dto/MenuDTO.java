package com.alexistdev.geobill.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuDTO {
    private String id;
    private String name;
    private String urlink;
    private String icon;
    private String parent;
    private String classlink;
    private String sortOrder;
}
