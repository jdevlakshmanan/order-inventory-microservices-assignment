package com.dev.inventory.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBatchDto {

    private Long id;
    private String sku;
    private int quantity;
    private LocalDate expiryDate;
}

