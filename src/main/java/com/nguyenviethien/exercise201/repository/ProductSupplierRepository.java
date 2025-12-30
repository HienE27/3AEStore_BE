package com.nguyenviethien.exercise201.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.entity.ProductSupplier;
import com.nguyenviethien.exercise201.entity.ProductSupplierId;
import com.nguyenviethien.exercise201.entity.Supplier;

@Repository
public interface ProductSupplierRepository extends JpaRepository<ProductSupplier, ProductSupplierId> {
    List<ProductSupplier> findByProduct(Product product);

    List<ProductSupplier> findBySupplier(Supplier supplier);

    void deleteByProductAndSupplier(Product product, Supplier supplier);
}