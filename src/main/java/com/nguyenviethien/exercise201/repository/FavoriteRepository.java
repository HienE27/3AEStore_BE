package com.nguyenviethien.exercise201.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nguyenviethien.exercise201.entity.Favorite;
import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.Product;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByCustomer(Customer customer);
    List<Favorite> findByCustomerId(UUID customerId);
    boolean existsByCustomerAndProduct(Customer customer, Product product);
    void deleteByCustomerAndProduct(Customer customer, Product product);
}


