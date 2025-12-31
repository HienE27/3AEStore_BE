package com.nguyenviethien.exercise201.service.impl;

import com.nguyenviethien.exercise201.entity.News;
import com.nguyenviethien.exercise201.repository.NewsRepository;
import com.nguyenviethien.exercise201.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Override
    public News createNews(News news) {
        System.out.println("💾 Saving news: " + news.getTitle());
        News savedNews = newsRepository.save(news);
        System.out.println("✅ Saved with ID: " + savedNews.getId());
        return savedNews;
    }

    @Override
    public News updateNews(Long id, News news) {
        System.out.println("🔍 Looking for news ID: " + id);
        News existingNews = newsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("News with ID " + id + " not found"));
        
        System.out.println("✅ Found existing news: " + existingNews.getTitle());
        existingNews.setTitle(news.getTitle());
        existingNews.setSummary(news.getSummary());
        existingNews.setContent(news.getContent());
        existingNews.setImage(news.getImage());
        existingNews.setAuthor(news.getAuthor());
        
        News updatedNews = newsRepository.save(existingNews);
        System.out.println("✅ Updated news: " + updatedNews.getTitle());
        return updatedNews;
    }

    @Override
    public void deleteNews(Long id) {
        System.out.println("🗑️ Deleting news ID: " + id);
        if (!newsRepository.existsById(id)) {
            throw new RuntimeException("News with ID " + id + " not found");
        }
        newsRepository.deleteById(id);
        System.out.println("✅ Deleted news ID: " + id);
    }

    @Override
    public News getNewsById(Long id) {
        System.out.println("🔍 Fetching news by ID: " + id);
        News news = newsRepository.findById(id).orElse(null);
        
        if (news != null) {
            System.out.println("✅ Found news: " + news.getTitle());
            System.out.println("📊 Details: Author=" + news.getAuthor() + ", Created=" + news.getCreatedAt());
            System.out.println("🖼️ Has image: " + (news.getImage() != null && !news.getImage().trim().isEmpty()));
        } else {
            System.out.println("❌ News with ID " + id + " not found in database");
            
            // Debug: Show all available IDs
            List<News> allNews = newsRepository.findAll();
            System.out.println("🔍 Available news IDs: " + 
                allNews.stream().map(n -> n.getId().toString()).toList());
        }
        

        return news;
        //return newsRepository.findById(id).orElse(null);
    }

    @Override
    public List<News> getAllNews() {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_20\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"NewsServiceImpl.java:76\",\"message\":\"getAllNews entry\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
            fw.close();
        } catch (java.io.IOException ex) {}
        // #endregion
        try {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_21\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"NewsServiceImpl.java:78\",\"message\":\"Before calling newsRepository.findAll\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            List<News> newsList = newsRepository.findAll();
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_22\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"NewsServiceImpl.java:80\",\"message\":\"After calling newsRepository.findAll\",\"data\":{\"newsCount\":\"" + (newsList != null ? newsList.size() : 0) + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            System.out.println("📊 Retrieved " + newsList.size() + " news items");
            return newsList;
        } catch (Exception e) {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_23\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"NewsServiceImpl.java:82\",\"message\":\"Exception in getAllNews\",\"data\":{\"error\":\"" + e.getClass().getName() + "\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
                fw.close();
            } catch (java.io.IOException ex) {}
            // #endregion
            throw e;
        }
    }
}