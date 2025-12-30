package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.entity.ProductTag;
import com.nguyenviethien.exercise201.entity.Tag;
import com.nguyenviethien.exercise201.repository.ProductTagRepository;
import com.nguyenviethien.exercise201.repository.TagRepository;
import com.nguyenviethien.exercise201.service.TagService;

@Service
@Transactional
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ProductTagRepository productTagRepository;

    @Override
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    @Override
    public Optional<Tag> findById(UUID id) {
        return tagRepository.findById(id);
    }

    @Override
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }

    @Override
    public void deleteById(UUID id) {
        tagRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return tagRepository.existsById(id);
    }

    @Override
    public boolean existsByTagName(String tagName) {
        return tagRepository.existsByTagName(tagName);
    }

    @Override
    @Transactional
    public void addProductToTag(Tag tag, Product product) {
        ProductTag productTag = new ProductTag();
        productTag.setTag(tag);
        productTag.setProduct(product);
        productTagRepository.save(productTag);
    }

    @Override
    @Transactional
    public void removeProductFromTag(Tag tag, Product product) {
        productTagRepository.deleteByProductAndTag(product, tag);
    }
}