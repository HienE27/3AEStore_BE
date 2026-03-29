package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Customer;
import java.util.List;

@Repository
@RepositoryRestResource(path = "customer")
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    // Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE c.user_name = :user_name AND c.deleted = false")
    Customer findByUser_name(@Param("user_name") String user_name);

    @Query("SELECT c FROM Customer c WHERE c.user_name = :user_name AND c.deleted = false")
    Optional<Customer> findByUser_nameOptional(@Param("user_name") String user_name);

    // Thêm query method trong CustomerRepository để chỉ lấy customer chưa bị xóa
   @Query("SELECT c FROM Customer c WHERE c.deleted = false")
    Page<Customer> findAllActive(Pageable pageable);


    @Query("SELECT c FROM Customer c WHERE c.id = :id AND c.deleted = false")
    Optional<Customer> findActiveById(@Param("id") UUID id);

    // Find by email
    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);
    
    // Find by email containing (case insensitive)
    List<Customer> findByEmailContainingIgnoreCase(String email);
    
    // Custom query for search
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.first_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.last_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Customer> searchByTerm(@Param("searchTerm") String searchTerm);
}