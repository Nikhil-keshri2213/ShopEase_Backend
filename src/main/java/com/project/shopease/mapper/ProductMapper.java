package com.project.shopease.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.shopease.dto.ProductDto;
import com.project.shopease.dto.ProductResourceDto;
import com.project.shopease.dto.ProductVariantDto;
import com.project.shopease.entities.Category;
import com.project.shopease.entities.CategoryType;
import com.project.shopease.entities.Product;
import com.project.shopease.entities.ProductVariant;
import com.project.shopease.entities.Resources;
import com.project.shopease.services.CategoryService;

@Component
public class ProductMapper {

    @Autowired
    private CategoryService categoryService;

    public Product mapToProductEntity(ProductDto productDto) {
        Product product = new Product();

        if (null != productDto.getId()) {
            product.setId(productDto.getId());
        }

        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setBrand(productDto.getBrand());
        product.setNewArrival(productDto.isNewArrival());
        product.setPrice(productDto.getPrice());
        product.setRating(productDto.getRating());
        product.setSlug(productDto.getSlug());

        Category category = categoryService.getCategory(productDto.getCategoryId());

        if (null != category) {
            product.setCategory(category);
            UUID categoryTypeId = productDto.getCategoryTypeId();

            CategoryType categoryType = category.getCategoryTypes().stream()
                    .filter(categoryType1 -> categoryType1.getId().equals(categoryTypeId)).findFirst().orElse(null);
            product.setCategoryType(categoryType);
        }

        if (null != productDto.getVariants()) {
            product.setProductVariants(mapToProductVariant(productDto.getVariants(), product));
        }
        if (null != productDto.getProductResource()) {
            product.setResources(mapToProductResources(productDto.getProductResource(), product));
        }
        return product;
    }

    public List<ProductVariant> mapToProductVariant(List<ProductVariantDto> productVariantDto, Product product) {
        return productVariantDto.stream().map(productVariantDto1 -> {
            ProductVariant productVariant = new ProductVariant();

            if (null != productVariantDto1.getId()) {
                productVariant.setId(productVariantDto1.getId());
            }

            productVariant.setColor(productVariantDto1.getColor());
            productVariant.setSize(productVariantDto1.getSize());
            productVariant.setStockQuantity(productVariantDto1.getStockQuantity());
            productVariant.setProduct(product);
            return productVariant;
        }).collect(Collectors.toList());
    }

    public List<Resources> mapToProductResources(List<ProductResourceDto> productResource, Product product) {
        return productResource.stream().map(productResourceDto -> {
            Resources resources = new Resources();

            if (null != productResourceDto.getId()) {
                resources.setId(productResourceDto.getId());
            }

            resources.setName(productResourceDto.getName());
            resources.setType(productResourceDto.getType());
            resources.setUrl(productResourceDto.getUrl());
            resources.setIsPrimary(productResourceDto.getIsPrimary());
            resources.setProduct(product);
            return resources;
        }).collect(Collectors.toList());
    }

    public List<ProductDto> getProductDtos(List<Product> products) {
        return products.stream().map(this::mapProductDto).toList();
    }

    public ProductDto mapProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .brand(product.getBrand())
                .name(product.getName())
                .price(product.getPrice())
                .isNewArrival(product.isNewArrival())
                .slug(product.getSlug())
                .rating(product.getRating())
                .description(product.getDescription())
                .thumbnail(getProductThumbnail(product.getResources())).build();
    }

    private String getProductThumbnail(List<Resources> resources) {
        if (resources == null || resources.isEmpty()) {
            return null;
        }

        return resources.stream()
                .filter(Resources::getIsPrimary)
                .findFirst()
                .map(Resources::getUrl)
                .orElse(null);
    }

    public List<ProductVariantDto> mapToProductVariantListToDto(List<ProductVariant> productVariants) {
        return productVariants.stream().map(this::mapToProductVariantDto).toList();
    }

    private ProductVariantDto mapToProductVariantDto(ProductVariant productVariant) {
        return ProductVariantDto.builder()
                .color(productVariant.getColor())
                .id(productVariant.getId())
                .size(productVariant.getSize())
                .stockQuantity(productVariant.getStockQuantity())
                .build();
    }

    public List<ProductResourceDto> mapProductResourcesListDto(List<Resources> resources) {
        return resources.stream().map(this::mapResourceToDto).toList();
    }

    private ProductResourceDto mapResourceToDto(Resources resources) {
        return ProductResourceDto.builder()
                .id(resources.getId())
                .name(resources.getName())
                .url(resources.getUrl())
                .type(resources.getType())
                .isPrimary(resources.getIsPrimary())
                .build();
    }
}
