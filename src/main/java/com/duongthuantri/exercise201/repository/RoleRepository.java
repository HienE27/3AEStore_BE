package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
}