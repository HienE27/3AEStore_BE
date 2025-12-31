package com.nguyenviethien.exercise201.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.entity.Category;
import com.nguyenviethien.exercise201.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_1\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"CategoryController.java:24\",\"message\":\"getAllCategories entry\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
            fw.close();
        } catch (java.io.IOException ex) {}
        // #endregion
        try {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_2\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"CategoryController.java:26\",\"message\":\"Before calling categoryService.findAll\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            List<Category> categories = categoryService.findAll();
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_3\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"CategoryController.java:28\",\"message\":\"After calling categoryService.findAll\",\"data\":{\"categoryCount\":\"" + (categories != null ? categories.size() : 0) + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            
            // Format response theo Spring Data REST format để match với frontend
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_4\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"CategoryController.java:32\",\"message\":\"Before building response\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> embedded = new HashMap<>();
            embedded.put("categories", categories);
            response.put("_embedded", embedded);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_5\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"CategoryController.java:36\",\"message\":\"Exception in getAllCategories\",\"data\":{\"error\":\"" + e.getClass().getName() + "\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\",\"stackTrace\":\"" + java.util.Arrays.toString(e.getStackTrace()).replace("\"", "'").substring(0, Math.min(500, java.util.Arrays.toString(e.getStackTrace()).replace("\"", "'").length())) + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            System.err.println("💥 Error getting all categories: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable UUID id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/root")
    public ResponseEntity<List<Category>> getRootCategories() {
        return ResponseEntity.ok(categoryService.findByParentIsNull());
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<Category>> getSubcategories(@PathVariable UUID id) {
        return categoryService.findById(id)
                .map(category -> ResponseEntity.ok(categoryService.findByParent(category)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category, @RequestParam UUID staffId) {
    try {
        Category savedCategory = categoryService.save(category, staffId);
        return ResponseEntity.ok(savedCategory);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
        @PathVariable UUID id,
        @RequestBody Category category) {
    // Kiểm tra xem Category có tồn tại không
    Category existingCategory = categoryService.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));

    // Kiểm tra vòng lặp vô hạn (category không thể là parent của chính nó)
    if (category.getParent() != null && category.getParent().getId().equals(id)) {
        return ResponseEntity.badRequest()
                .body("Category cannot be its own parent");
    }

    // Kiểm tra trùng tên category
    if (!category.getCategoryName().equals(existingCategory.getCategoryName()) &&
            categoryService.existsByCategoryName(category.getCategoryName())) {
        return ResponseEntity.badRequest()
                .body("Category with this name already exists");
    }

    // Cập nhật các thuộc tính của Category hiện tại
    existingCategory.setCategoryName(category.getCategoryName());
    existingCategory.setCategoryDescription(category.getCategoryDescription());
    existingCategory.setParent(category.getParent());
    // Các trường khác bạn muốn cập nhật...

    // Lưu lại category đã được cập nhật
    Category updatedCategory = categoryService.saveAll(existingCategory); // Dùng save để cập nhật

    // Trả về phản hồi thành công với category đã cập nhật
    return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        if (!categoryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoryService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}