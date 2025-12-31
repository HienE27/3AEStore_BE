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
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_6\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"CategoryServiceImpl.java:28\",\"message\":\"findAll entry\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
            fw.close();
        } catch (java.io.IOException ex) {}
        // #endregion
        try {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_7\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"CategoryServiceImpl.java:30\",\"message\":\"Before calling categoryRepository.findAll\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            List<Category> result = categoryRepository.findAll();
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_8\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"CategoryServiceImpl.java:32\",\"message\":\"After calling categoryRepository.findAll\",\"data\":{\"categoryCount\":\"" + (result != null ? result.size() : 0) + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            return result;
        } catch (Exception e) {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_9\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"CategoryServiceImpl.java:34\",\"message\":\"Exception in findAll\",\"data\":{\"error\":\"" + e.getClass().getName() + "\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            throw e;
        }
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