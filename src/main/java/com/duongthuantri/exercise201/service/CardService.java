package com.duongthuantri.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.duongthuantri.exercise201.entity.Card;
import com.duongthuantri.exercise201.entity.CardItem;
import com.duongthuantri.exercise201.entity.Customer;
import com.fasterxml.jackson.databind.JsonNode;

public interface CardService {
    List<Card> getAllCards();

    public ResponseEntity<?> save(JsonNode jsonNode);

    public ResponseEntity<?> updateQuantity(UUID customerId, UUID productId, int newQuantity);

    public ResponseEntity<?> removeProductFromCart(UUID customerId, UUID productId);
}