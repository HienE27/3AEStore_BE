package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Country;
import com.nguyenviethien.exercise201.entity.Supplier;
import com.nguyenviethien.exercise201.repository.SupplierRepository;
import com.nguyenviethien.exercise201.service.SupplierService;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    @Override
    public Optional<Supplier> findById(UUID id) {
        return supplierRepository.findById(id);
    }

    @Override
    public List<Supplier> findByCountry(Country country) {
        return supplierRepository.findByCountry(country);
    }

    @Override
    public List<Supplier> findByCompanyContaining(String company) {
        return supplierRepository.findByCompanyContaining(company);
    }

    @Override
    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @Override
    public void deleteById(UUID id) {
        supplierRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return supplierRepository.existsById(id);
    }

    @Override
    public boolean existsBySupplierName(String supplierName) {
        return supplierRepository.existsBySupplierName(supplierName);
    }
}