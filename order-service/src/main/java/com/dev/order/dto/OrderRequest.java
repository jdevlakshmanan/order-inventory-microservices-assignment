package com.dev.order.dto;

import lombok.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotNull
    private String customerId;

    @NotEmpty
    private List<OrderLine> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderLine {

        @NotNull
        private String sku;

        @NotNull
        private Integer quantity;
    }
}
