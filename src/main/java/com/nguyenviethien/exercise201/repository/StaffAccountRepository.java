package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Role;
import com.nguyenviethien.exercise201.entity.StaffAccount;

@Repository
@RepositoryRestResource(path = "staff")
public interface StaffAccountRepository extends JpaRepository<StaffAccount, UUID> {
    Optional<StaffAccount> findByEmail(String email);

    // Fetch role eagerly to avoid lazy proxy initialization outside session
    @Query("SELECT c FROM StaffAccount c LEFT JOIN FETCH c.role WHERE c.user_name = :user_name")
    StaffAccount findByUser_name(@Param("user_name") String user_name);

    @Query("SELECT s.role FROM StaffAccount s WHERE s.id = :id")
    Role findRoleByStaffId(@Param("id") UUID id);
    
}