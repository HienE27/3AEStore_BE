package com.nguyenviethien.exercise201.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Trả về 204 cho /favicon.ico để tránh 500 khi trình duyệt tự gọi (backend không có file favicon).
 */
@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
