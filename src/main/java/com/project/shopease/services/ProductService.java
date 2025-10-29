package com.project.shopease.services;

import java.util.List;
import java.util.UUID;

import org.apache.coyote.BadRequestException;

import com.project.shopease.dto.ProductDto;
import com.project.shopease.entities.Product;

public interface 
ProductService {
  public Product addProduct(ProductDto productDto);  
  public List<ProductDto> getAllProducts(UUID categoryId, UUID categoryTypeId);
  public ProductDto getProductBySlug(String slug);
  public ProductDto getProductById(UUID id);
  public Product updateProduct(ProductDto productdto);
  public Product fetchProductById(UUID id) throws BadRequestException;
  public void deleteProduct(UUID id);
} 