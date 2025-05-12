package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    boolean existsByTagName(String tagName);
}