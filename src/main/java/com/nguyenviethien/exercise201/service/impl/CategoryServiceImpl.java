package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Category;
import com.nguyenviethien.exercise201.entity.StaffAccount;
import com.nguyenviethien.exercise201.repository.CategoryRepository;
import com.nguyenviethien.exercise201.repository.StaffAccountRepository;
import com.nguyenviethien.exercise201.service.CategoryService;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
    //
    @Autowired
    private StaffAccountRepository staffAccountRepository;

    //
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<Category> findByParentIsNull() {
        return categoryRepository.findByParentIsNull();
    }

    @Override
    public List<Category> findByParent(Category parent) {
        return categoryRepository.findByParent(parent);
    }
    @Override
    public Category saveAll(Category category) {
        return categoryRepository.save(category);
    }
    @Override
    public Category save(Category category, UUID staffId) {
        StaffAccount staff = staffAccountRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));
            category.setCreated_at(new java.util.Date(System.currentTimeMillis()));
            category.setUpdated_at(new java.util.Date(System.currentTimeMillis()));
            category.setActive(true);
            category.setCreatedBy(staff);
            category.setUpdatedBy(staff);
        return categoryRepository.save(category);
    }

    @Override
    public void deleteById(UUID id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return categoryRepository.existsById(id);
    }

    @Override
    public boolean existsByCategoryName(String categoryName) {
        return categoryRepository.existsByCategoryName(categoryName);
    }
}