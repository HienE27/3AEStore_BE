package com.nguyenviethien.exercise201.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class TestController {

    // Test cơ bản
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("🧪 TEST ENDPOINT CALLED");
        return ResponseEntity.ok("Test endpoint works! " + System.currentTimeMillis());
    }

    // Test với path variable
    @GetMapping("/test/{id}")
    public ResponseEntity<String> testWithId(@PathVariable Long id) {
        System.out.println("🧪 TEST WITH ID: " + id);
        return ResponseEntity.ok("Test with ID works! ID: " + id);
    }

    // Test API news path
    @GetMapping("/api/test")
    public ResponseEntity<String> testApi() {
        System.out.println("🧪 API TEST ENDPOINT CALLED");
        return ResponseEntity.ok("API Test works!");
    }

    // Test API news với ID
    @GetMapping("/api/test/{id}")
    public ResponseEntity<String> testApiWithId(@PathVariable Long id) {
        System.out.println("🧪 API TEST WITH ID: " + id);
        return ResponseEntity.ok("API Test with ID works! ID: " + id);
    }
}