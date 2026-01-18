package com.alexistdev.geobill.request;

import com.alexistdev.geobill.config.ValidationConstant;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductRequest {

    @Nullable
    private UUID id;

    @NotEmpty(message = ValidationConstant.productTypeRequired)
    @Size(max=255, message = ValidationConstant.productTypeMax)
    private String productTypeId;

    @NotBlank(message = ValidationConstant.nameNotNull)
    @Size(max=150, message = ValidationConstant.nameMax)
    private String name;

    @NotNull(message = ValidationConstant.priceNotNull)
    private Double price;

    @NotNull(message = ValidationConstant.cycleNotNull)
    private Integer cycle;

    @Nullable
    @Size(max=255, message = ValidationConstant.nameMax255)
    private String capacity;

    @Nullable
    @Size(max=255, message = ValidationConstant.nameMax255)
    private String bandwith;

    @Nullable
    @Size(max=255, message = ValidationConstant.nameMax255)
    private String addon_domain;

    @Nullable
    @Size(max=255, message = ValidationConstant.nameMax255)
    private String database_account;

    @Nullable
    @Size(max=255, message = ValidationConstant.nameMax255)
    private String ftp_account;

    @Nullable
    @Size(max=255, message = ValidationConstant.nameMax255)
    private String info1;

    @Nullable
    @Size(max=255, message = ValidationConstant.nameMax255)
    private String info2;

    @Nullable
    @Size(max=255, message = ValidationConstant.nameMax255)
    private String info3;

    @Nullable
    @Size(max=255, message = ValidationConstant.nameMax255)
    private String info4;

    @Nullable
    @Size(max=255, message = ValidationConstant.nameMax255)
    private String info5;

}
