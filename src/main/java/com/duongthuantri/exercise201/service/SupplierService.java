package com.duongthuantri.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.duongthuantri.exercise201.entity.Country;
import com.duongthuantri.exercise201.entity.Supplier;

public interface SupplierService {
    List<Supplier> findAll();

    Optional<Supplier> findById(UUID id);

    List<Supplier> findByCountry(Country country);

    List<Supplier> findByCompanyContaining(String company);

    Supplier save(Supplier supplier);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsBySupplierName(String supplierName);
}