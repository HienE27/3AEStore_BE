package com.nguyenviethien.exercise201.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.entity.Favorite;
import com.nguyenviethien.exercise201.service.FavoriteService;
import com.nguyenviethien.exercise201.service.JWT.JwtService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private JwtService jwtService;

    // ========== Token-based endpoints (for authenticated users) ==========
    
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavoritesForCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            String token = authorization.substring(7);
            UUID cid = jwtService.extractId(token);
            if (cid == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
            }
            List<Favorite> favs = favoriteService.getFavoritesByCustomerId(cid);
            List<String> productIds = favs.stream()
                    .map(f -> f.getProduct().getId().toString())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("favorites", productIds));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/favorites")
    public ResponseEntity<?> addFavoriteForCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorization, @RequestBody Map<String, String> body) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            String token = authorization.substring(7);
            UUID cid = jwtService.extractId(token);
            if (cid == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
            }
            UUID productId = UUID.fromString(body.get("productId"));
            Favorite fav = favoriteService.addFavorite(cid, productId);
            return ResponseEntity.ok(Map.of("success", true, "favoriteId", fav != null ? fav.getId().toString() : null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/favorites/{productId}")
    public ResponseEntity<?> removeFavoriteForCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable String productId) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            String token = authorization.substring(7);
            UUID cid = jwtService.extractId(token);
            if (cid == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
            }
            UUID pid = UUID.fromString(productId);
            favoriteService.removeFavorite(cid, pid);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Customer-based endpoints (for guest users with customerId) ==========
    
    @GetMapping("/customers/{customerId}/favorites")
    public ResponseEntity<?> getFavorites(@PathVariable String customerId) {
        try {
            UUID cid = UUID.fromString(customerId);
            List<Favorite> favs = favoriteService.getFavoritesByCustomerId(cid);
            List<String> productIds = favs.stream()
                    .map(f -> f.getProduct().getId().toString())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("favorites", productIds));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/customers/{customerId}/favorites")
    public ResponseEntity<?> addFavorite(@PathVariable String customerId, @RequestBody Map<String, String> body) {
        try {
            UUID cid = UUID.fromString(customerId);
            UUID productId = UUID.fromString(body.get("productId"));
            Favorite fav = favoriteService.addFavorite(cid, productId);
            return ResponseEntity.ok(Map.of("success", true, "favoriteId", fav != null ? fav.getId().toString() : null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/customers/{customerId}/favorites/{productId}")
    public ResponseEntity<?> removeFavorite(@PathVariable String customerId, @PathVariable String productId) {
        try {
            UUID cid = UUID.fromString(customerId);
            UUID pid = UUID.fromString(productId);
            favoriteService.removeFavorite(cid, pid);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
