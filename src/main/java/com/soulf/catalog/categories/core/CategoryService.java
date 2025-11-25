package com.soulf.catalog.categories.core;

import com.soulf.catalog.categories.core.domain.Category;
import com.soulf.catalog.categories.core.repository.CategoryRepository;
import com.soulf.catalog.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        log.info("Fetching all active categories");
        return categoryRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Category> getRootCategories() {
        log.info("Fetching root categories");
        return categoryRepository.findByParentCategoryIsNullAndIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        log.info("Fetching category with id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Category getCategoryBySlug(String slug) {
        log.info("Fetching category with slug: {}", slug);
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
    }

    @Transactional
    public Category createCategory(String name, String description, String slug, Long parentCategoryId) {
        log.info("Creating new category: {}", name);

        if (categoryRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Category with name " + name + " already exists");
        }

        Category category = Category.builder()
                .name(name)
                .description(description)
                .slug(slug != null && !slug.isEmpty() ? slug : generateSlug(name))
                .build();

        if (parentCategoryId != null) {
            Category parentCategory = categoryRepository.findById(parentCategoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + parentCategoryId));
            category.setParentCategory(parentCategory);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", savedCategory.getId());
        return savedCategory;
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description, String slug, Long parentCategoryId) {
        log.info("Updating category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getName().equals(name) && categoryRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Category with name " + name + " already exists");
        }

        category.setName(name);
        category.setDescription(description);
        if (!category.getName().equals(name)) {
            category.setSlug(generateSlug(name));
        } else if (slug != null && !slug.isEmpty()) {
            category.setSlug(slug);
        }

        if (parentCategoryId != null) {
            Category parentCategory = categoryRepository.findById(parentCategoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + parentCategoryId));
            category.setParentCategory(parentCategory);
        } else {
            category.setParentCategory(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", updatedCategory.getId());
        return updatedCategory;
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with id: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

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
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setIsActive(false);
        categoryRepository.save(category);
        log.info("Category deactivated successfully with id: {}", id);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}

