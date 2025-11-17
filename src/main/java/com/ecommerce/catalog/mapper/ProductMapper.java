package com.ecommerce.catalog.mapper;

import com.ecommerce.catalog.dto.ProductAttributeDTO;
import com.ecommerce.catalog.dto.ProductDTO;
import com.ecommerce.catalog.dto.ProductImageDTO;
import com.ecommerce.catalog.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "availableQuantity", ignore = true)
    ProductDTO toDTO(Product product);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductDTO productDTO);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(ProductDTO productDTO, @MappingTarget Product product);

    List<ProductDTO> toDTOList(List<Product> products);

    ProductAttributeDTO toAttributeDTO(com.ecommerce.catalog.model.ProductAttribute attribute);
    List<ProductAttributeDTO> toAttributeDTOList(List<com.ecommerce.catalog.model.ProductAttribute> attributes);

    ProductImageDTO toImageDTO(com.ecommerce.catalog.model.ProductImage image);
    List<ProductImageDTO> toImageDTOList(List<com.ecommerce.catalog.model.ProductImage> images);
}

