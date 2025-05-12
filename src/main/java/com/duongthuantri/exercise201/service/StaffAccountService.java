package com.duongthuantri.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.duongthuantri.exercise201.entity.StaffAccount;

public interface StaffAccountService {
    List<StaffAccount> findAll();

    Optional<StaffAccount> findById(UUID id);

    Optional<StaffAccount> findByEmail(String email);

    StaffAccount save(StaffAccount staffAccount);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    
}