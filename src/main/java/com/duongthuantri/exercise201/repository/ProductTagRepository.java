package com.duongthuantri.exercise201.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Product;
import com.duongthuantri.exercise201.entity.ProductTag;
import com.duongthuantri.exercise201.entity.ProductTagId;
import com.duongthuantri.exercise201.entity.Tag;

import java.util.List;

@Repository
public interface ProductTagRepository extends JpaRepository<ProductTag, ProductTagId> {
    List<ProductTag> findByProduct(Product product);

    List<ProductTag> findByTag(Tag tag);

    void deleteByProductAndTag(Product product, Tag tag);
}