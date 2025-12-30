package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.ShippingRate;
import com.nguyenviethien.exercise201.entity.ShippingZone;

@Repository
public interface ShippingRateRepository extends JpaRepository<ShippingRate, UUID> {
    List<ShippingRate> findByShippingZone(ShippingZone shippingZone);

    List<ShippingRate> findByShippingZoneAndWeightUnit(ShippingZone shippingZone, ShippingRate.WeightUnit weightUnit);

    void deleteByShippingZone(ShippingZone shippingZone);
}