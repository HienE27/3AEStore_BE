package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.ProductAttributeValue;
import com.nguyenviethien.exercise201.entity.Variant;
import com.nguyenviethien.exercise201.entity.VariantValue;

@Repository
public interface VariantValueRepository extends JpaRepository<VariantValue, UUID> {
    List<VariantValue> findByVariant(Variant variant);

    List<VariantValue> findByProductAttributeValue(ProductAttributeValue productAttributeValue);

    void deleteByVariantAndProductAttributeValue(Variant variant, ProductAttributeValue productAttributeValue);
}