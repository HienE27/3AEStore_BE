package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Customer;
import com.duongthuantri.exercise201.entity.CustomerAddress;

@Repository
@RepositoryRestResource(path = "customerAddress")
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {
    List<CustomerAddress> findByCustomer(Customer customer);

    // Tìm địa chỉ theo customerId
    List<CustomerAddress> findByCustomerId(UUID customerId);
    
}