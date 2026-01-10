package com.alexistdev.geobill.request;


import com.alexistdev.geobill.config.ValidationConstant;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductTypeRequest {

    @Nullable
    private UUID id;

    @NotEmpty(message = ValidationConstant.nameNotNull)
    @Size(max=150, message = ValidationConstant.nameMax)
    private String name;

}
