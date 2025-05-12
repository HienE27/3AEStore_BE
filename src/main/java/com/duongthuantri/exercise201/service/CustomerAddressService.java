package com.duongthuantri.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.duongthuantri.exercise201.entity.Customer;
import com.duongthuantri.exercise201.entity.CustomerAddress;

public interface CustomerAddressService {
    List<CustomerAddress> findAll();

    Optional<CustomerAddress> findById(UUID id);

    List<CustomerAddress> findByCustomer(Customer customer);

    CustomerAddress save(CustomerAddress address);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}