package com.duongthuantri.exercise201.repository;

import java.util.List;
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

import com.duongthuantri.exercise201.entity.Product;

@RepositoryRestResource(path = "products")
public interface ProductRepository extends JpaRepository<Product, UUID> {
      //tìm kiếm sản phâm có tồn tại không
      Boolean existsByProductName(String productName);

      //TÌm kiếm sản phẩm theo tên sản phẩm
      Page<Product> findByProductNameContaining(@RequestParam("productName") String productName, Pageable pageable);

      //Tìm tất cã sản phẩm có giá sale
      @RestResource(path = "salePriceGreaterThanZero", rel = "salePriceGreaterThanZero")
      @Query(value = "SELECT * FROM products WHERE sale_price > 0", nativeQuery = true)
      Page<Product> findAllWithSalePriceGreaterThanZero(Pageable pageable);

      //Tìm 3 sản phẩm mới nhất
      List<Product> findTop3ByOrderByCreatedAtDesc();
}