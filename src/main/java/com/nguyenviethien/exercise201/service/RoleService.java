package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nguyenviethien.exercise201.entity.Role;

public interface RoleService {
    List<Role> findAll();

    Optional<Role> findById(UUID id);

    Role save(Role role);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}