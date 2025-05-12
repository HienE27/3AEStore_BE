package com.duongthuantri.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duongthuantri.exercise201.entity.Slideshow;
import com.duongthuantri.exercise201.repository.SlideshowRepository;
import com.duongthuantri.exercise201.service.SlideshowService;

@Service
@Transactional
public class SlideshowServiceImpl implements SlideshowService {

    @Autowired
    private SlideshowRepository slideshowRepository;

    @Override
    public List<Slideshow> findAll() {
        return slideshowRepository.findAll();
    }

    @Override
    public Optional<Slideshow> findById(UUID id) {
        return slideshowRepository.findById(id);
    }

    @Override
    public List<Slideshow> findPublishedSlideshowsOrdered() {
        return slideshowRepository.findByPublishedTrueOrderByDisplayOrderAsc();
    }

    @Override
    public List<Slideshow> findAllOrdered() {
        return slideshowRepository.findByOrderByDisplayOrderAsc();
    }

    @Override
    public Slideshow save(Slideshow slideshow) {
        return slideshowRepository.save(slideshow);
    }

    @Override
    public void deleteById(UUID id) {
        slideshowRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return slideshowRepository.existsById(id);
    }

    @Override
    @Transactional
    public void incrementClicks(UUID id) {
        findById(id).ifPresent(slideshow -> {
            slideshow.setClicks(slideshow.getClicks() + 1);
            save(slideshow);
        });
    }
}