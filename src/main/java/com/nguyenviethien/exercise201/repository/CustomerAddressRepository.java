package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.CustomerAddress;

@Repository
@RepositoryRestResource(path = "customerAddress")
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {
    List<CustomerAddress> findByCustomer(Customer customer);

    // Tìm địa chỉ theo customerId
    // List<CustomerAddress> findByCustomerId(UUID customerId);


    @Query("SELECT ca FROM CustomerAddress ca WHERE ca.customer.id = :customerId ORDER BY ca.id DESC")
    List<CustomerAddress> findByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT ca FROM CustomerAddress ca WHERE ca.customer.id = :customerId ORDER BY ca.id DESC LIMIT 1")
    CustomerAddress findLatestByCustomerId(@Param("customerId") UUID customerId);
    
}