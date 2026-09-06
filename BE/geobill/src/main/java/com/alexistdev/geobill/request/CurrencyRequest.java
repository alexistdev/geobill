package com.alexistdev.geobill.request;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CurrencyRequest {

    @Nullable
    private UUID id;
    private String name;
    private String symbol;
    private String code;
    private Double exchangeRate;
    private boolean isDefault;
}
