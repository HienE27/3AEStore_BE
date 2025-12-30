package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.AttributeValue;
import com.nguyenviethien.exercise201.entity.ProductAttribute;
import com.nguyenviethien.exercise201.entity.ProductAttributeValue;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, UUID> {
    List<ProductAttributeValue> findByProductAttribute(ProductAttribute productAttribute);

    List<ProductAttributeValue> findByAttributeValue(AttributeValue attributeValue);

    void deleteByProductAttributeAndAttributeValue(ProductAttribute productAttribute, AttributeValue attributeValue);
}