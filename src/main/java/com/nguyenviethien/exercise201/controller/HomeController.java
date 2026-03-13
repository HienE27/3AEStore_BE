package com.nguyenviethien.exercise201.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main HomeController that returns hero slide data for the frontend.
 * This implementation returns a small list of slides that can later be
 * replaced with data from a database or a CMS.
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    @Value("${app.frontend.host:/}")
    private String frontendHost;

    @GetMapping("/hero")
    public ResponseEntity<List<Map<String, Object>>> getHeroSlides() {
        List<Map<String, Object>> slides = new ArrayList<>();

        Map<String, Object> slide1 = new HashMap<>();
        slide1.put("id", "hero-1");
        slide1.put("title", "Khám Phá Tri Thức Mới");
        slide1.put("subtitle", "Mùa Hè Sôi Động 2024");
        slide1.put("description", "Hàng ngàn đầu sách mới nhất đã cập bến. Giảm giá lên đến 50% cho thành viên mới ngay hôm nay.");
        slide1.put("image", "/images/banners/banner1.jpg");
        slide1.put("ctaText", "Mua Ngay");
        slide1.put("ctaLink", "/promotions");

        Map<String, Object> slide2 = new HashMap<>();
        slide2.put("id", "hero-2");
        slide2.put("title", "Khuyến Mãi Hấp Dẫn");
        slide2.put("subtitle", "Giảm giá theo mùa");
        slide2.put("description", "Đừng bỏ lỡ chương trình khuyến mãi cuối tuần với nhiều ưu đãi lớn.");
        slide2.put("image", "/images/banners/banner2.jpg");
        slide2.put("ctaText", "Xem Khuyến Mãi");
        slide2.put("ctaLink", "/promotions");

        slides.add(slide1);
        slides.add(slide2);

        return ResponseEntity.ok(slides);
    }
}


