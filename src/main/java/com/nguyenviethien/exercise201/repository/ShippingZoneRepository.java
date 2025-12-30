package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.ShippingZone;

@Repository
public interface ShippingZoneRepository extends JpaRepository<ShippingZone, UUID> {
    List<ShippingZone> findByActiveTrue();

    List<ShippingZone> findByFreeShippingTrue();

    boolean existsByName(String name);
}