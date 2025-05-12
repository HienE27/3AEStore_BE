package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Attribute;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, UUID> {
    boolean existsByAttributeName(String attributeName);
}