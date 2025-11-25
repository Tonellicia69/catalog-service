package com.soulf.catalog.products.api;

import com.soulf.catalog.products.api.dto.ProductAttributeDTO;
import com.soulf.catalog.products.api.dto.ProductDTO;
import com.soulf.catalog.products.api.dto.ProductImageDTO;
import com.soulf.catalog.products.core.ProductService;
import com.soulf.catalog.products.core.commands.CreateProductCommand;
import com.soulf.catalog.products.core.commands.ProductAttributeCommand;
import com.soulf.catalog.products.core.commands.ProductImageCommand;
import com.soulf.catalog.products.core.commands.UpdateProductCommand;
import com.soulf.catalog.products.core.domain.Product;
import com.soulf.catalog.products.core.domain.ProductAttribute;
import com.soulf.catalog.products.core.domain.ProductImage;
import com.soulf.catalog.products.infrastructure.provider.InventoryServiceProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final InventoryServiceProvider inventoryServiceProvider;

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products.map(this::toDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(toDTO(product));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        Product product = productService.getProductBySku(sku);
        return ResponseEntity.ok(toDTO(product));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isVisible,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        Page<Product> products = productService.searchProducts(
                name, categoryId, minPrice, maxPrice, isActive, isVisible, pageable);
        return ResponseEntity.ok(products.map(this::toDTO));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        Page<Product> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products.map(this::toDTO));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        CreateProductCommand command = toCreateCommand(productDTO);
        Product product = productService.createProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        UpdateProductCommand command = toUpdateCommand(productDTO);
        Product product = productService.updateProduct(id, command);
        return ResponseEntity.ok(toDTO(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = ProductDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .inventoryId(product.getInventoryId())
                .isActive(product.getIsActive())
                .isVisible(product.getIsVisible())
                .availableQuantity(enrichWithInventory(product.getInventoryId()))
                .build();

        if (product.getAttributes() != null) {
            dto.setAttributes(product.getAttributes().stream()
                    .map(this::toAttributeDTO)
                    .collect(Collectors.toList()));
        }

        if (product.getImages() != null) {
            dto.setImages(product.getImages().stream()
                    .map(this::toImageDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private ProductAttributeDTO toAttributeDTO(ProductAttribute attribute) {
        return ProductAttributeDTO.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .value(attribute.getValue())
                .displayOrder(attribute.getDisplayOrder())
                .build();
    }

    private ProductImageDTO toImageDTO(ProductImage image) {
        return ProductImageDTO.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .isPrimary(image.getIsPrimary())
                .displayOrder(image.getDisplayOrder())
                .build();
    }

    private CreateProductCommand toCreateCommand(ProductDTO dto) {
        CreateProductCommand.CreateProductCommandBuilder builder = CreateProductCommand.builder()
                .sku(dto.getSku())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .categoryId(dto.getCategoryId())
                .inventoryId(dto.getInventoryId())
                .isActive(dto.getIsActive())
                .isVisible(dto.getIsVisible());

        if (dto.getAttributes() != null) {
            builder.attributes(dto.getAttributes().stream()
                    .map(attr -> ProductAttributeCommand.builder()
                            .name(attr.getName())
                            .value(attr.getValue())
                            .displayOrder(attr.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList()));
        }

        if (dto.getImages() != null) {
            builder.images(dto.getImages().stream()
                    .map(img -> ProductImageCommand.builder()
                            .imageUrl(img.getImageUrl())
                            .altText(img.getAltText())
                            .isPrimary(img.getIsPrimary())
                            .displayOrder(img.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    private UpdateProductCommand toUpdateCommand(ProductDTO dto) {
        UpdateProductCommand.UpdateProductCommandBuilder builder = UpdateProductCommand.builder()
                .sku(dto.getSku())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .categoryId(dto.getCategoryId())
                .inventoryId(dto.getInventoryId())
                .isActive(dto.getIsActive())
                .isVisible(dto.getIsVisible());

        if (dto.getAttributes() != null) {
            builder.attributes(dto.getAttributes().stream()
                    .map(attr -> ProductAttributeCommand.builder()
                            .name(attr.getName())
                            .value(attr.getValue())
                            .displayOrder(attr.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList()));
        }

        if (dto.getImages() != null) {
            builder.images(dto.getImages().stream()
                    .map(img -> ProductImageCommand.builder()
                            .imageUrl(img.getImageUrl())
                            .altText(img.getAltText())
                            .isPrimary(img.getIsPrimary())
                            .displayOrder(img.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    private Integer enrichWithInventory(Long inventoryId) {
        if (inventoryId == null) {
            return null;
        }
        return inventoryServiceProvider.getAvailableQuantity(inventoryId);
    }
}

