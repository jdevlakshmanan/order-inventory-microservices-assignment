package com.dev.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateRequest {

    @NotNull
    private String sku;

    @NotNull
    private int delta;

    private String strategy;
}
