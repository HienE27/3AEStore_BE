package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Product;

@RepositoryRestResource(path = "products")
public interface ProductRepository extends JpaRepository<Product, UUID> {
      //tìm kiếm sản phâm có tồn tại không
      Boolean existsByProductName(String productName);

      //TÌm kiếm sản phẩm theo tên sản phẩm
      Product findByproductNameContaining(String productName);
}