package com.nguyenviethien.exercise201.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import com.nguyenviethien.exercise201.entity.Product;

@RepositoryRestResource(path = "products")
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    // ✅ THÊM METHOD NÀY - Lấy product với categories
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productCategories pc LEFT JOIN FETCH pc.category WHERE p.id = :productId")
    Optional<Product> findByIdWithCategories(@Param("productId") UUID productId);
    
    //tìm kiếm sản phẩm có tồn tại không
    Boolean existsByProductName(String productName);

    //TÌm kiếm sản phẩm theo tên sản phẩm
    Page<Product> findByProductNameContaining(@RequestParam("productName") String productName, Pageable pageable);

    //Tìm tất cã sản phẩm có giá sale
    @RestResource(path = "salePriceGreaterThanZero", rel = "salePriceGreaterThanZero")
    @Query(value = "SELECT * FROM products WHERE sale_price > 0", nativeQuery = true)
    Page<Product> findAllWithSalePriceGreaterThanZero(Pageable pageable);

    //Tìm 3 sản phẩm mới nhất
    List<Product> findTop3ByOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT p FROM Product p JOIN p.productCategories pc WHERE pc.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") UUID categoryId);



    List<Product> findByPublishedTrue();
    
    @Query("SELECT p FROM Product p WHERE " +
           "p.productName LIKE %:keyword% OR " +
           "p.shortDescription LIKE %:keyword% OR " +
           "p.sku LIKE %:keyword%")
    List<Product> searchProducts(@Param("keyword") String keyword);
    
    // Check stock availability
    @Query("SELECT p.quantity FROM Product p WHERE p.id = :productId")
    Optional<Integer> getStockQuantity(@Param("productId") UUID productId);
    
    // Find by slug
    Optional<Product> findBySlug(String slug);
    
    // Find by price range
    @Query("SELECT p FROM Product p WHERE p.salePrice BETWEEN :minPrice AND :maxPrice AND p.published = true")
    List<Product> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, 
                                 @Param("maxPrice") java.math.BigDecimal maxPrice);
}