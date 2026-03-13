package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.UUID;

import com.nguyenviethien.exercise201.entity.Favorite;

public interface FavoriteService {
    List<Favorite> getFavoritesByCustomerId(UUID customerId);
    Favorite addFavorite(UUID customerId, UUID productId);
    void removeFavorite(UUID customerId, UUID productId);
    boolean isFavorited(UUID customerId, UUID productId);
}


