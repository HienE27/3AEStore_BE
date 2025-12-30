package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Category;

@RepositoryRestResource(path = "categories")
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByParentIsNull();

    List<Category> findByParent(Category parent);

    boolean existsByCategoryName(String categoryName);

    //lấy tất cã category của product
}