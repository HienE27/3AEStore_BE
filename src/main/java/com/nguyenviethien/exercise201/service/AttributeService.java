package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nguyenviethien.exercise201.entity.Attribute;
import com.nguyenviethien.exercise201.entity.AttributeValue;

public interface AttributeService {
    List<Attribute> findAll();

    Optional<Attribute> findById(UUID id);

    Attribute save(Attribute attribute);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsByAttributeName(String attributeName);

    AttributeValue addAttributeValue(Attribute attribute, String value, String color);

    void removeAttributeValue(AttributeValue attributeValue);
}