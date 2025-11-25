package com.soulf.catalog.categories.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private String slug;
    private Long parentCategoryId;
    private String parentCategoryName;

    private Boolean isActive;
    private Integer displayOrder;

    @Builder.Default
    private List<CategoryDTO> subCategories = new ArrayList<>();
}

