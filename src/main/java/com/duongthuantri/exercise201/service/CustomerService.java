package com.duongthuantri.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.duongthuantri.exercise201.entity.Customer;
import com.fasterxml.jackson.databind.JsonNode;

public interface CustomerService {
    List<Customer> findAll();

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByEmail(String email);

    Customer save(Customer customer);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    public Customer update(UUID id, Customer updatedCustomer);

    boolean existsByEmail(String email);
    
    // public ResponseEntity<?> register(Customer customer);
}