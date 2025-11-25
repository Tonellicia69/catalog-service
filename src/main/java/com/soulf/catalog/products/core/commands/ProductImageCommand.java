package com.soulf.catalog.products.core.commands;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageCommand {
    private String imageUrl;
    private String altText;
    private Boolean isPrimary;
    private Integer displayOrder;
}

