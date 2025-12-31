package com.nguyenviethien.exercise201.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ensure trailing slash
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String resourceLocation = "file:" + uploadPath.toString() + "/";
        // Map /uploads/** to the filesystem uploads folder
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
        // Also accept requests that include a "reviews" subpath but store files at uploads root
        registry.addResourceHandler("/uploads/reviews/**")
                .addResourceLocations(resourceLocation);
    }
}


