package com.duongthuantri.exercise201.controller;

import com.duongthuantri.exercise201.DTO.TokenInfoResponse;
import com.duongthuantri.exercise201.service.JWT.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
public class TokenController {
    @Autowired
    private JwtService jwtService;

    @GetMapping("/info")
    public TokenInfoResponse getTokenInfo(@RequestParam("token") String token) {
        String username = jwtService.extractUsername(token);
        String role = jwtService.extractClaims(token, claims -> claims.get("role", String.class));
        java.util.Date expiration = jwtService.extractExpiration(token);
        return new TokenInfoResponse(username, role, expiration);
    }
}
