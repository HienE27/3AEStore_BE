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
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_15\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"NewsController.java:20\",\"message\":\"getAllNews entry\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
            fw.close();
        } catch (java.io.IOException ex) {}
        // #endregion
        try {
            System.out.println("=== LẤY TẤT CẢ TIN TỨC ===");
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_16\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"NewsController.java:23\",\"message\":\"Before calling newsService.getAllNews\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            List<News> newsList = newsService.getAllNews();
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_17\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"NewsController.java:25\",\"message\":\"After calling newsService.getAllNews\",\"data\":{\"newsCount\":\"" + (newsList != null ? newsList.size() : 0) + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            System.out.println("Tìm thấy " + newsList.size() + " tin tức");
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_18\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"NewsController.java:27\",\"message\":\"Before returning response\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            return ResponseEntity.ok(newsList);
        } catch (Exception e) {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_19\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"NewsController.java:29\",\"message\":\"Exception in getAllNews\",\"data\":{\"error\":\"" + e.getClass().getName() + "\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\",\"stackTrace\":\"" + java.util.Arrays.toString(e.getStackTrace()).replace("\"", "'").substring(0, Math.min(500, java.util.Arrays.toString(e.getStackTrace()).replace("\"", "'").length())) + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            System.err.println("💥 Error getting all news: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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