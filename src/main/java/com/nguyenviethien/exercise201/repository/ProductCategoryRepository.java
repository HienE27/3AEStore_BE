package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Category;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.entity.ProductCategory;

@Repository
@RepositoryRestResource(path = "productCategories")
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    List<ProductCategory> findByProduct(Product product);

    List<ProductCategory> findByCategory(Category category);

    void deleteByProductAndCategory(Product product, Category category);


    @Modifying
    @Transactional
    @Query("DELETE FROM ProductCategory pc WHERE pc.product.id = :productId")
    void deleteAllByProductId(UUID productId);
}