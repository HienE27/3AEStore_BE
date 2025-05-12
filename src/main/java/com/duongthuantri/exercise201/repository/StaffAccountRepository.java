package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Role;
import com.duongthuantri.exercise201.entity.StaffAccount;

@Repository
@RepositoryRestResource(path = "staff")
public interface StaffAccountRepository extends JpaRepository<StaffAccount, UUID> {
    Optional<StaffAccount> findByEmail(String email);

    @Query("SELECT c FROM StaffAccount c WHERE c.user_name = :user_name")
    StaffAccount findByUser_name(@Param("user_name") String user_name);

    @Query("SELECT s.role FROM StaffAccount s WHERE s.id = :id")
    Role findRoleByStaffId(@Param("id") UUID id);
    
}