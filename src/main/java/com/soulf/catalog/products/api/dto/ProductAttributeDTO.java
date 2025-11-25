package com.soulf.catalog.products.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeDTO {

    private Long id;

    @NotBlank(message = "Attribute name is required")
    private String name;

    private String value;
    private Integer displayOrder;
}

