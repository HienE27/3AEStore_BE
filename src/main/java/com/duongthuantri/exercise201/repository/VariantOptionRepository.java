package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Product;
import com.duongthuantri.exercise201.entity.VariantOption;

@Repository
public interface VariantOptionRepository extends JpaRepository<VariantOption, UUID> {
    List<VariantOption> findByProduct(Product product);

    List<VariantOption> findByProductAndActiveTrue(Product product);
}