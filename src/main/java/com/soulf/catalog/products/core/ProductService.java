package com.soulf.catalog.products.core;

import com.soulf.catalog.categories.core.domain.Category;
import com.soulf.catalog.categories.core.repository.CategoryRepository;
import com.soulf.catalog.products.core.commands.CreateProductCommand;
import com.soulf.catalog.products.core.commands.ProductAttributeCommand;
import com.soulf.catalog.products.core.commands.ProductImageCommand;
import com.soulf.catalog.products.core.commands.UpdateProductCommand;
import com.soulf.catalog.products.core.domain.Product;
import com.soulf.catalog.products.core.domain.ProductAttribute;
import com.soulf.catalog.products.core.domain.ProductImage;
import com.soulf.catalog.products.core.repository.ProductRepository;
import com.soulf.catalog.products.infrastructure.provider.InventoryServiceProvider;
import com.soulf.catalog.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryServiceProvider inventoryServiceProvider;

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination");
        return productRepository.findByIsActiveTrueAndIsVisibleTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        log.info("Fetching product with SKU: {}", sku);
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(
            String name,
            Long categoryId,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            Boolean isActive,
            Boolean isVisible,
            Pageable pageable) {
        log.info("Searching products with filters: name={}, categoryId={}, minPrice={}, maxPrice={}",
                name, categoryId, minPrice, maxPrice);
        return productRepository.searchProducts(name, categoryId, minPrice, maxPrice, isActive, isVisible, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.info("Fetching products for category id: {}", categoryId);
        return productRepository.findByCategoryIdAndIsActiveTrueAndIsVisibleTrue(categoryId, pageable);
    }

    @Transactional
    public Product createProduct(CreateProductCommand command) {
        log.info("Creating new product with SKU: {}", command.getSku());

        if (productRepository.findBySku(command.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + command.getSku() + " already exists");
        }

        Product product = Product.builder()
                .sku(command.getSku())
                .name(command.getName())
                .description(command.getDescription())
                .price(command.getPrice())
                .inventoryId(command.getInventoryId())
                .isActive(command.getIsActive() != null ? command.getIsActive() : true)
                .isVisible(command.getIsVisible() != null ? command.getIsVisible() : true)
                .build();

        if (command.getCategoryId() != null) {
            Category category = categoryRepository.findById(command.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + command.getCategoryId()));
            product.setCategory(category);
        }

        if (command.getAttributes() != null) {
            List<ProductAttribute> attributes = command.getAttributes().stream()
                    .map(attrCmd -> ProductAttribute.builder()
                            .product(product)
                            .name(attrCmd.getName())
                            .value(attrCmd.getValue())
                            .displayOrder(attrCmd.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList());
            product.setAttributes(attributes);
        }

        if (command.getImages() != null) {
            List<ProductImage> images = command.getImages().stream()
                    .map(imgCmd -> ProductImage.builder()
                            .product(product)
                            .imageUrl(imgCmd.getImageUrl())
                            .altText(imgCmd.getAltText())
                            .isPrimary(imgCmd.getIsPrimary())
                            .displayOrder(imgCmd.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList());
            product.setImages(images);
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Long id, UpdateProductCommand command) {
        log.info("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (!product.getSku().equals(command.getSku()) &&
                productRepository.findBySku(command.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + command.getSku() + " already exists");
        }

        product.setSku(command.getSku());
        product.setName(command.getName());
        product.setDescription(command.getDescription());
        product.setPrice(command.getPrice());
        product.setInventoryId(command.getInventoryId());
        if (command.getIsActive() != null) {
            product.setIsActive(command.getIsActive());
        }
        if (command.getIsVisible() != null) {
            product.setIsVisible(command.getIsVisible());
        }

        if (command.getCategoryId() != null) {
            Category category = categoryRepository.findById(command.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + command.getCategoryId()));
            product.setCategory(category);
        } else if (command.getCategoryId() == null && product.getCategory() != null) {
            product.setCategory(null);
        }

        if (command.getAttributes() != null) {
            product.getAttributes().clear();
            command.getAttributes().forEach(attrCmd -> {
                ProductAttribute attribute = ProductAttribute.builder()
                        .product(product)
                        .name(attrCmd.getName())
                        .value(attrCmd.getValue())
                        .displayOrder(attrCmd.getDisplayOrder())
                        .build();
                product.getAttributes().add(attribute);
            });
        }

        if (command.getImages() != null) {
            product.getImages().clear();
            command.getImages().forEach(imgCmd -> {
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(imgCmd.getImageUrl())
                        .altText(imgCmd.getAltText())
                        .isPrimary(imgCmd.getIsPrimary())
                        .displayOrder(imgCmd.getDisplayOrder())
                        .build();
                product.getImages().add(image);
            });
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with id: {}", updatedProduct.getId());
        return updatedProduct;
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
        log.info("Product deleted successfully with id: {}", id);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        log.info("Deactivating product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setIsActive(false);
        productRepository.save(product);
        log.info("Product deactivated successfully with id: {}", id);
    }
}

