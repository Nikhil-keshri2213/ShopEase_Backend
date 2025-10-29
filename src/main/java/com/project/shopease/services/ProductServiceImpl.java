package com.project.shopease.services;

import java.util.List;
import java.util.UUID;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.project.shopease.dto.ProductDto;
import com.project.shopease.entities.Product;
import com.project.shopease.exceptions.ResourceNotFoundEx;
import com.project.shopease.mapper.ProductMapper;
import com.project.shopease.repositories.ProductRepository;
import com.project.shopease.specification.ProductSpecification;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Product addProduct(ProductDto productDto) {
        Product product = productMapper.mapToProductEntity(productDto);
        return productRepository.save(product);
    }

    @Override
    public List<ProductDto> getAllProducts(UUID categoryId, UUID categoryTypeId) {

        Specification<Product> productSpecification = Specification.where(null);

        if (null != categoryId) {
            productSpecification = productSpecification.and(ProductSpecification.hasCategoryId(categoryId));
        }
        if (null != categoryTypeId) {
            productSpecification = productSpecification.and(ProductSpecification.hasCategoryTypeId(categoryTypeId));
        }
        List<Product> products = productRepository.findAll(productSpecification);
        return productMapper.getProductDtos(products);
    }

    @Override
    public ProductDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug);
        if (null == product) {
            throw new ResourceNotFoundEx("Not Found");
        }

        ProductDto productDto = productMapper.mapProductDto(product);
        productDto.setCategoryId(product.getCategory().getId());
        productDto.setCategoryTypeId(product.getCategoryType().getId());
        productDto.setVariants(productMapper.mapToProductVariantListToDto(product.getProductVariants()));
        productDto.setProductResource(productMapper.mapProductResourcesListDto(product.getResources()));
        return productDto;
    }

    @Override
    public ProductDto getProductById(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundEx("Not Found"));
        ProductDto productDto = productMapper.mapProductDto(product);
        productDto.setCategoryId(product.getCategory().getId());
        productDto.setCategoryTypeId(product.getCategoryType().getId());
        productDto.setVariants(productMapper.mapToProductVariantListToDto(product.getProductVariants()));
        productDto.setProductResource(productMapper.mapProductResourcesListDto(product.getResources()));
        return productDto;
    }

    @Override
    public Product updateProduct(ProductDto productDto) {
        Product existing = productRepository.findById(productDto.getId()).orElseThrow(() -> new ResourceNotFoundEx("Product Not Found for Updation"));
        Product updated = productMapper.mapToProductEntity(productDto);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setId(existing.getId());
        return productRepository.save(updated);
    }

    @Override
    public Product fetchProductById(UUID id) throws BadRequestException {
        return productRepository.findById(id).orElseThrow(BadRequestException::new);
    }

    @Override
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundEx("Product Not Found for Deletion"));
        productRepository.delete(product);
    }

    // @Override
    // public Product updateProduct(ProductDto productDto, UUID id) {
    // Product product= productRepository.findById(id).orElseThrow(()-> new
    // ResourceNotFoundEx("Product Not Found!"));
    // productDto.setId(product.getId());
    // return productRepository.save(productMapper.mapToProductEntity(productDto));
    // }

    // @Override
    // public Product fetchProductById(UUID id) throws Exception {
    // return productRepository.findById(id).orElseThrow(BadRequestException::new);
    // }
}