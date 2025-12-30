package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nguyenviethien.exercise201.entity.Category;

public interface CategoryService {
    List<Category> findAll();

    Optional<Category> findById(UUID id);

    List<Category> findByParentIsNull();

    List<Category> findByParent(Category parent);

    Category saveAll(Category category);
    
    Category save(Category category, UUID staffId);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsByCategoryName(String categoryName);
}