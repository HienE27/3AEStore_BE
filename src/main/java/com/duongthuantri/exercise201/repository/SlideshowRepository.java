package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Slideshow;

@Repository
@RepositoryRestResource(path = "slideshows")
public interface SlideshowRepository extends JpaRepository<Slideshow, UUID> {
    List<Slideshow> findByPublishedTrueOrderByDisplayOrderAsc();

    List<Slideshow> findByOrderByDisplayOrderAsc();
}