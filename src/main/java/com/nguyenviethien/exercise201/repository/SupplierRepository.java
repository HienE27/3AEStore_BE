package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Country;
import com.nguyenviethien.exercise201.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    List<Supplier> findByCountry(Country country);

    boolean existsBySupplierName(String supplierName);

    List<Supplier> findByCompanyContaining(String company);
}