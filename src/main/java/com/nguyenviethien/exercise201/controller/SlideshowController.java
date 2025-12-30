package com.nguyenviethien.exercise201.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.entity.Slideshow;
import com.nguyenviethien.exercise201.service.SlideshowService;

@RestController
@RequestMapping("/api/slideshows")
public class SlideshowController {

    @Autowired
    private SlideshowService slideshowService;

    @GetMapping
    public ResponseEntity<List<Slideshow>> getAllSlideshows() {
        return ResponseEntity.ok(slideshowService.findAll());
    }

    @GetMapping("/published")
    public ResponseEntity<List<Slideshow>> getPublishedSlideshows() {
        return ResponseEntity.ok(slideshowService.findPublishedSlideshowsOrdered());
    }

    @GetMapping("/ordered")
    public ResponseEntity<List<Slideshow>> getAllSlideshowsOrdered() {
        return ResponseEntity.ok(slideshowService.findAllOrdered());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Slideshow> getSlideshowById(@PathVariable UUID id) {
        return slideshowService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Slideshow> createSlideshow(@RequestBody Slideshow slideshow) {
        return ResponseEntity.ok(slideshowService.save(slideshow));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Slideshow> updateSlideshow(
            @PathVariable UUID id,
            @RequestBody Slideshow slideshow) {
        if (!slideshowService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        slideshow.setId(id);
        return ResponseEntity.ok(slideshowService.save(slideshow));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlideshow(@PathVariable UUID id) {
        if (!slideshowService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        slideshowService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/increment-clicks")
    public ResponseEntity<Void> incrementClicks(@PathVariable UUID id) {
        if (!slideshowService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        slideshowService.incrementClicks(id);
        return ResponseEntity.ok().build();
    }
}