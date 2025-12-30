package com.nguyenviethien.exercise201.service;

import com.nguyenviethien.exercise201.entity.News;
import java.util.List;

public interface NewsService {
    News createNews(News news);

    News updateNews(Long id, News news);

    void deleteNews(Long id);

    News getNewsById(Long id);

    List<News> getAllNews();
}
