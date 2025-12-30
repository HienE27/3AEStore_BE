package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nguyenviethien.exercise201.entity.Slideshow;

public interface SlideshowService {
    List<Slideshow> findAll();

    Optional<Slideshow> findById(UUID id);

    List<Slideshow> findPublishedSlideshowsOrdered();

    List<Slideshow> findAllOrdered();

    Slideshow save(Slideshow slideshow);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    void incrementClicks(UUID id);
}