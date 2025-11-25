package com.soulf.catalog.products.core.commands;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductAttributeCommand {
    private String name;
    private String value;
    private Integer displayOrder;
}

