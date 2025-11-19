package com.ecommerce.catalog.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.catalog.dto.CategoryDTO;
import com.ecommerce.catalog.exception.ResourceNotFoundException;
import com.ecommerce.catalog.mapper.CategoryMapper;
import com.ecommerce.catalog.model.Category;
import com.ecommerce.catalog.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.info("Fetching all active categories");
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return categoryMapper.toDTOList(categories);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getRootCategories() {
        log.info("Fetching root categories");
        List<Category> categories = categoryRepository.findByParentCategoryIsNullAndIsActiveTrue();
        return buildCategoryTree(categories);
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        log.info("Fetching category with id: {}", id);
        Objects.requireNonNull(id, "id must not be null");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return buildCategoryTree(category);
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryBySlug(String slug) {
        log.info("Fetching category with slug: {}", slug);
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return buildCategoryTree(category);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.info("Creating new category: {}", categoryDTO.getName());

        // Check if name already exists
        if (categoryRepository.findByName(categoryDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Category with name " + categoryDTO.getName() + " already exists");
        }

        Category category = categoryMapper.toEntity(categoryDTO);

        // Generate slug if not provided
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(generateSlug(categoryDTO.getName()));
        }

        // Set parent category if provided
        if (categoryDTO.getParentCategoryId() != null) {
            Long parentId = categoryDTO.getParentCategoryId();
            Objects.requireNonNull(parentId, "parentCategoryId must not be null");
            Category parentCategory = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + parentId));
            category.setParentCategory(parentCategory);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", savedCategory.getId());

        return buildCategoryTree(savedCategory);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        log.info("Updating category with id: {}", id);

        Objects.requireNonNull(id, "id must not be null");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check name uniqueness if changed
        if (!category.getName().equals(categoryDTO.getName()) &&
                categoryRepository.findByName(categoryDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Category with name " + categoryDTO.getName() + " already exists");
        }

        categoryMapper.updateEntityFromDTO(categoryDTO, category);

        // Update slug if name changed
        if (!category.getName().equals(categoryDTO.getName())) {
            category.setSlug(generateSlug(categoryDTO.getName()));
        }

        // Update parent category if provided
        if (categoryDTO.getParentCategoryId() != null) {
            Long parentId = categoryDTO.getParentCategoryId();
            Objects.requireNonNull(parentId, "parentCategoryId must not be null");
            Category parentCategory = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + parentId));
            category.setParentCategory(parentCategory);
        } else if (categoryDTO.getParentCategoryId() == null && category.getParentCategory() != null) {
            category.setParentCategory(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", updatedCategory.getId());

        return buildCategoryTree(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with id: {}", id);
        Objects.requireNonNull(id, "id must not be null");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if category has products
        if (!category.getSubCategories().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete category with subcategories. Please delete or move subcategories first.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with id: {}", id);
    }

    @Transactional
    public void deactivateCategory(Long id) {
        log.info("Deactivating category with id: {}", id);
        Objects.requireNonNull(id, "id must not be null");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setIsActive(false);
        categoryRepository.save(category);
        log.info("Category deactivated successfully with id: {}", id);
    }

    private CategoryDTO buildCategoryTree(Category category) {
        CategoryDTO categoryDTO = categoryMapper.toDTO(category);

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

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
