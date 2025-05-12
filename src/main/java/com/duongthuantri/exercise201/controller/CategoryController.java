package com.duongthuantri.exercise201.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.duongthuantri.exercise201.entity.Category;
import com.duongthuantri.exercise201.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.findAll());
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