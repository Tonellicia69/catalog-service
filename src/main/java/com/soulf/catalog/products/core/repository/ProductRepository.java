package com.soulf.catalog.products.core.repository;

import com.soulf.catalog.products.core.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    Page<Product> findByIsActiveTrueAndIsVisibleTrue(Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrueAndIsVisibleTrue(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive) AND " +
           "(:isVisible IS NULL OR p.isVisible = :isVisible)")
    Page<Product> searchProducts(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("isActive") Boolean isActive,
            @Param("isVisible") Boolean isVisible,
            Pageable pageable
    );

    List<Product> findByInventoryIdIn(List<Long> inventoryIds);
}

