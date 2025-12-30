package com.nguyenviethien.exercise201.repository;

import com.nguyenviethien.exercise201.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
}
