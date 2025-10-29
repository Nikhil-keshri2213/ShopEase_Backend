package com.project.shopease.repositories;

import java.util.UUID;
import com.project.shopease.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product>{

    Product findBySlug(String slug);
    
}
