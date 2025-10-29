package com.project.shopease.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.shopease.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>{
    
}
