package com.ecommerce.catalog.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.catalog.client.InventoryServiceClient;
import com.ecommerce.catalog.dto.InventoryResponseDTO;
import com.ecommerce.catalog.dto.ProductDTO;
import com.ecommerce.catalog.exception.ResourceNotFoundException;
import com.ecommerce.catalog.mapper.ProductMapper;
import com.ecommerce.catalog.model.Category;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.model.ProductAttribute;
import com.ecommerce.catalog.model.ProductImage;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final InventoryServiceClient inventoryServiceClient;

    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination");
        Page<Product> products = productRepository.findByIsActiveTrueAndIsVisibleTrue(pageable);
        return products.map(this::enrichWithInventory);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        Objects.requireNonNull(id, "id must not be null");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        Objects.requireNonNull(product, "product must not be null");
        return enrichWithInventory(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        log.info("Fetching product with SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return enrichWithInventory(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(
            String name,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isActive,
            Boolean isVisible,
            Pageable pageable) {
        log.info("Searching products with filters: name={}, categoryId={}, minPrice={}, maxPrice={}",
                name, categoryId, minPrice, maxPrice);

        Page<Product> products = productRepository.searchProducts(
                name, categoryId, minPrice, maxPrice, isActive, isVisible, pageable);

        return products.map(this::enrichWithInventory);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.info("Fetching products for category id: {}", categoryId);
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        Page<Product> products = productRepository.findByCategoryIdAndIsActiveTrueAndIsVisibleTrue(
                categoryId, pageable);
        return products.map(this::enrichWithInventory);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product with SKU: {}", productDTO.getSku());

        // Check if SKU already exists
        if (productRepository.findBySku(productDTO.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + productDTO.getSku() + " already exists");
        }

        Product product = productMapper.toEntity(productDTO);

        // Set category if provided
        if (productDTO.getCategoryId() != null) {
            Long catId = productDTO.getCategoryId();
            Objects.requireNonNull(catId, "categoryId must not be null");
            Category category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + catId));
            product.setCategory(category);
        }

        // Map attributes
        if (productDTO.getAttributes() != null) {
            List<ProductAttribute> attributes = productDTO.getAttributes().stream()
                    .map(attrDTO -> ProductAttribute.builder()
                            .product(product)
                            .name(attrDTO.getName())
                            .value(attrDTO.getValue())
                            .displayOrder(attrDTO.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList());
            product.setAttributes(attributes);
        }

        // Map images
        if (productDTO.getImages() != null) {
            List<ProductImage> images = productDTO.getImages().stream()
                    .map(imgDTO -> ProductImage.builder()
                            .product(product)
                            .imageUrl(imgDTO.getImageUrl())
                            .altText(imgDTO.getAltText())
                            .isPrimary(imgDTO.getIsPrimary())
                            .displayOrder(imgDTO.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList());
            product.setImages(images);
        }

        Product savedProduct = productRepository.save(product);
        Objects.requireNonNull(savedProduct, "saved product must not be null");
        log.info("Product created successfully with id: {}", savedProduct.getId());

        return enrichWithInventory(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with id: {}", id);

        Objects.requireNonNull(id, "id must not be null");

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check SKU uniqueness if changed
        if (!product.getSku().equals(productDTO.getSku()) &&
                productRepository.findBySku(productDTO.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + productDTO.getSku() + " already exists");
        }

        productMapper.updateEntityFromDTO(productDTO, product);

        // Update category if provided
        if (productDTO.getCategoryId() != null) {
            Long catId = productDTO.getCategoryId();
            Objects.requireNonNull(catId, "categoryId must not be null");
            Category category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + catId));
            product.setCategory(category);
        } else if (productDTO.getCategoryId() == null && product.getCategory() != null) {
            product.setCategory(null);
        }

        // Update attributes
        if (productDTO.getAttributes() != null) {
            product.getAttributes().clear();
            productDTO.getAttributes().forEach(attrDTO -> {
                ProductAttribute attribute = ProductAttribute.builder()
                        .product(product)
                        .name(attrDTO.getName())
                        .value(attrDTO.getValue())
                        .displayOrder(attrDTO.getDisplayOrder())
                        .build();
                product.getAttributes().add(attribute);
            });
        }

        // Update images
        if (productDTO.getImages() != null) {
            product.getImages().clear();
            productDTO.getImages().forEach(imgDTO -> {
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(imgDTO.getImageUrl())
                        .altText(imgDTO.getAltText())
                        .isPrimary(imgDTO.getIsPrimary())
                        .displayOrder(imgDTO.getDisplayOrder())
                        .build();
                product.getImages().add(image);
            });
        }

        Product updatedProduct = productRepository.save(product);
        Objects.requireNonNull(updatedProduct, "updated product must not be null");
        log.info("Product updated successfully with id: {}", updatedProduct.getId());

        return enrichWithInventory(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        Objects.requireNonNull(id, "id must not be null");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        Objects.requireNonNull(product, "product must not be null");
        productRepository.delete(product);
        log.info("Product deleted successfully with id: {}", id);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        log.info("Deactivating product with id: {}", id);
        Objects.requireNonNull(id, "id must not be null");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        Objects.requireNonNull(product, "product must not be null");
        product.setIsActive(false);
        productRepository.save(product);
        log.info("Product deactivated successfully with id: {}", id);
    }

    private ProductDTO enrichWithInventory(Product product) {
        ProductDTO productDTO = productMapper.toDTO(product);

        // Map attributes and images
        productDTO.setAttributes(productMapper.toAttributeDTOList(product.getAttributes()));
        productDTO.setImages(productMapper.toImageDTOList(product.getImages()));

        // Enrich with inventory data if inventoryId is present
        if (product.getInventoryId() != null) {
            try {
                InventoryResponseDTO inventory = inventoryServiceClient.getInventoryById(product.getInventoryId());
                if (inventory != null) {
                    productDTO.setAvailableQuantity(inventory.getAvailableQuantity());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch inventory data for product id: {}, inventoryId: {}. Error: {}",
                        product.getId(), product.getInventoryId(), e.getMessage());
            }
        }

        return productDTO;
    }
}
