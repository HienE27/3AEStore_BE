package com.duongthuantri.exercise201.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.duongthuantri.exercise201.entity.Country;
import com.duongthuantri.exercise201.entity.Supplier;
import com.duongthuantri.exercise201.service.CountryService;
import com.duongthuantri.exercise201.service.SupplierService;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private CountryService countryService;

    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable UUID id) {
        return supplierService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/country/{countryId}")
    public ResponseEntity<List<Supplier>> getSuppliersByCountry(
            @PathVariable Integer countryId) {
        return countryService.findById(countryId)
                .map(country -> ResponseEntity.ok(supplierService.findByCountry(country)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Supplier>> searchSuppliersByCompany(
            @RequestParam String company) {
        return ResponseEntity.ok(supplierService.findByCompanyContaining(company));
    }

    @PostMapping
    public ResponseEntity<?> createSupplier(@RequestBody Supplier supplier) {
        if (supplierService.existsBySupplierName(supplier.getSupplierName())) {
            return ResponseEntity.badRequest()
                    .body("Supplier name already exists");
        }
        return ResponseEntity.ok(supplierService.save(supplier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSupplier(
            @PathVariable UUID id,
            @RequestBody Supplier supplier) {
        if (!supplierService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!supplier.getSupplierName().equals(supplierService.findById(id).get().getSupplierName()) &&
                supplierService.existsBySupplierName(supplier.getSupplierName())) {
            return ResponseEntity.badRequest()
                    .body("Supplier name already exists");
        }
        supplier.setId(id);
        return ResponseEntity.ok(supplierService.save(supplier));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        if (!supplierService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        supplierService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}