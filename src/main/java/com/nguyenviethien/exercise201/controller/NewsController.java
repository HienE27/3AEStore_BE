package com.nguyenviethien.exercise201.controller;

import com.nguyenviethien.exercise201.entity.News;
import com.nguyenviethien.exercise201.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "false")
public class NewsController {

    @Autowired
    private NewsService newsService;

    // LẤY TẤT CẢ TIN TỨC
    @GetMapping("/api/news")
    public ResponseEntity<List<News>> getAllNews() {
        System.out.println("=== LẤY TẤT CẢ TIN TỨC ===");
        List<News> newsList = newsService.getAllNews();
        System.out.println("Tìm thấy " + newsList.size() + " tin tức");
        return ResponseEntity.ok(newsList);
    }

    // LẤY TIN TỨC THEO ID - ĐÃ SỬA
    @GetMapping("/api/news/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        System.out.println("=== LẤY TIN TỨC THEO ID: " + id + " ===");
        
        News news = newsService.getNewsById(id);
        if (news != null) {
            System.out.println("Tìm thấy tin tức: " + news.getTitle());
            return ResponseEntity.ok(news);
        } else {
            System.out.println("Không tìm thấy tin tức với ID: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    // TẠO TIN TỨC MỚI
    @PostMapping("/api/news")
    public ResponseEntity<News> createNews(@RequestBody News news) {
        System.out.println("=== TẠO TIN TỨC: " + news.getTitle() + " ===");
        News savedNews = newsService.createNews(news);
        return ResponseEntity.ok(savedNews);
    }

    // CẬP NHẬT TIN TỨC
    @PutMapping("/api/news/{id}")
    public ResponseEntity<News> updateNews(@PathVariable Long id, @RequestBody News news) {
        System.out.println("=== CẬP NHẬT TIN TỨC ID: " + id + " ===");
        try {
            News updatedNews = newsService.updateNews(id, news);
            return ResponseEntity.ok(updatedNews);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // XÓA TIN TỨC
    @DeleteMapping("/api/news/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        System.out.println("=== XÓA TIN TỨC ID: " + id + " ===");
        try {
            newsService.deleteNews(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}