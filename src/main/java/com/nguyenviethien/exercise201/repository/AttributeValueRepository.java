package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Attribute;
import com.nguyenviethien.exercise201.entity.AttributeValue;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, UUID> {
    List<AttributeValue> findByAttribute(Attribute attribute);

    boolean existsByAttributeAndAttributeValue(Attribute attribute, String attributeValue);
}