package com.zestindia.productapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {

    @NotNull(message = "quantity is required")
    @Min(value = 0, message = "quantity must not be negative")
    private Integer quantity;
}
