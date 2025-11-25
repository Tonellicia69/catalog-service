package com.soulf.catalog.products.core.commands;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CreateProductCommand {
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private Long inventoryId;
    private Boolean isActive;
    private Boolean isVisible;
    private List<ProductAttributeCommand> attributes;
    private List<ProductImageCommand> images;
}

