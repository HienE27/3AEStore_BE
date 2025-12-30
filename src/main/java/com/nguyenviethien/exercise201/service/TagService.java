package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.entity.Tag;

public interface TagService {
    List<Tag> findAll();

    Optional<Tag> findById(UUID id);

    Tag save(Tag tag);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsByTagName(String tagName);

    void addProductToTag(Tag tag, Product product);

    void removeProductFromTag(Tag tag, Product product);
}