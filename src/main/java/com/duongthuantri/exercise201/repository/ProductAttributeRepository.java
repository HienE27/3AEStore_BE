package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Attribute;
import com.duongthuantri.exercise201.entity.Product;
import com.duongthuantri.exercise201.entity.ProductAttribute;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, UUID> {
    List<ProductAttribute> findByProduct(Product product);

    List<ProductAttribute> findByAttribute(Attribute attribute);

    boolean existsByProductAndAttribute(Product product, Attribute attribute);
}