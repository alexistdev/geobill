package com.alexistdev.geobill.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrencyDTO {
    private String id;
    private String name;
    private String code;
    private String symbol;
    private Double exchangeRate;
    private boolean isDefault;
}
