package com.project.shopease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import com.project.shopease.entities.Product;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDetail 
{
    private UUID id;
    private Product product;
    private UUID productVariantId;
    private Integer quantity;
    private Double itemPrice;
}
