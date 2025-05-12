package com.duongthuantri.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.duongthuantri.exercise201.entity.Gallery;
import com.duongthuantri.exercise201.entity.Product;
import com.duongthuantri.exercise201.entity.VariantOption;

public interface VariantOptionService {
    List<VariantOption> findAll();

    Optional<VariantOption> findById(UUID id);

    List<VariantOption> findByProduct(Product product);

    List<VariantOption> findByProductAndActiveTrue(Product product);

    VariantOption save(VariantOption variantOption);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    void updateVariantImage(VariantOption variantOption, Gallery image);

    void toggleActive(UUID id);
}