package com.soulf.catalog.categories.api;

import com.soulf.catalog.categories.api.dto.CategoryDTO;
import com.soulf.catalog.categories.core.CategoryService;
import com.soulf.catalog.categories.core.domain.Category;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/roots")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        List<Category> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(buildCategoryTree(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(buildCategoryTree(category));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryDTO> getCategoryBySlug(@PathVariable String slug) {
        Category category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(buildCategoryTree(category));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        Category category = categoryService.createCategory(
                categoryDTO.getName(),
                categoryDTO.getDescription(),
                categoryDTO.getSlug(),
                categoryDTO.getParentCategoryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(buildCategoryTree(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        Category category = categoryService.updateCategory(
                id,
                categoryDTO.getName(),
                categoryDTO.getDescription(),
                categoryDTO.getSlug(),
                categoryDTO.getParentCategoryId());
        return ResponseEntity.ok(buildCategoryTree(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCategory(@PathVariable Long id) {
        categoryService.deactivateCategory(id);
        return ResponseEntity.noContent().build();
    }

    private CategoryDTO toDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .slug(category.getSlug())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .parentCategoryName(category.getParentCategory() != null ? category.getParentCategory().getName() : null)
                .isActive(category.getIsActive())
                .displayOrder(category.getDisplayOrder())
                .build();
    }

    private CategoryDTO buildCategoryTree(Category category) {
        CategoryDTO categoryDTO = toDTO(category);

        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            List<CategoryDTO> subCategoryDTOs = category.getSubCategories().stream()
                    .filter(Category::getIsActive)
                    .map(this::buildCategoryTree)
                    .collect(Collectors.toList());
            categoryDTO.setSubCategories(subCategoryDTOs);
        }

        return categoryDTO;
    }

    private List<CategoryDTO> buildCategoryTree(List<Category> categories) {
        return categories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }
}

