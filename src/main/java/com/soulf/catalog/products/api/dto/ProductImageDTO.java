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
public class ProductImageDTO {

    private Long id;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private String altText;
    private Boolean isPrimary;
    private Integer displayOrder;
}

