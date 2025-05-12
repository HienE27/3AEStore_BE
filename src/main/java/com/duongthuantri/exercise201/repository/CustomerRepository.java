package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Customer;
import java.util.List;


@Repository
@RepositoryRestResource(path = "customer")
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE c.user_name = :user_name")
    Customer findByUser_name(@Param("user_name") String user_name);
}