package com.soulf.catalog.products.infrastructure.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDTO {

    private Long inventoryId;
    private String sku;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalQuantity;
    private Boolean inStock;
}

