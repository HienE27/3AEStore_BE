package com.nguyenviethien.exercise201.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Controller để handle OAuth2 callback từ backend
 * Redirect về frontend với token và user info
 */
@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @Value("${APP_BASE_URL:http://localhost:3000}")
    private String frontendBaseUrl;

    /**
     * Handle OAuth2 callback - redirect về frontend
     * Endpoint này được gọi khi OAuth2 flow hoàn tất
     */
    @GetMapping("/callback")
    public void handleCallback(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String error,
            HttpServletResponse response) throws IOException {
        
        // Build redirect URL về frontend
        StringBuilder redirectUrl = new StringBuilder(frontendBaseUrl);
        redirectUrl.append("/oauth2/callback");
        
        if (token != null && userId != null) {
            // Success case - redirect với token và user info
            redirectUrl.append("?token=").append(URLEncoder.encode(token, StandardCharsets.UTF_8));
            redirectUrl.append("&userId=").append(URLEncoder.encode(userId, StandardCharsets.UTF_8));
            if (email != null) {
                redirectUrl.append("&email=").append(URLEncoder.encode(email, StandardCharsets.UTF_8));
            }
        } else if (error != null) {
            // Error case - redirect với error
            redirectUrl.append("?error=").append(URLEncoder.encode(error, StandardCharsets.UTF_8));
        } else {
            // Default error
            redirectUrl.append("?error=oauth_failed");
        }
        
        response.sendRedirect(redirectUrl.toString());
    }
}

