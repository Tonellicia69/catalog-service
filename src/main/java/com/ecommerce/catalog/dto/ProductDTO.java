package com.ecommerce.catalog.dto;

import com.ecommerce.catalog.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private Long categoryId;
    private String categoryName;

    private Long inventoryId;
    private Integer availableQuantity; // From inventory service

    private Boolean isActive;
    private Boolean isVisible;

    @Builder.Default
    private List<ProductAttributeDTO> attributes = new ArrayList<>();

    @Builder.Default
    private List<ProductImageDTO> images = new ArrayList<>();
}

