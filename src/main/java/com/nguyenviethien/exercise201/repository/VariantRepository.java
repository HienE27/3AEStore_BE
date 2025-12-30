package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.entity.Variant;
import com.nguyenviethien.exercise201.entity.VariantOption;

@Repository
public interface VariantRepository extends JpaRepository<Variant, UUID> {
    List<Variant> findByProduct(Product product);

    List<Variant> findByVariantOption(VariantOption variantOption);

    List<Variant> findByProductAndVariantOption(Product product, VariantOption variantOption);
}