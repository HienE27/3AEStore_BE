package com.duongthuantri.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.duongthuantri.exercise201.entity.ShippingRate;
import com.duongthuantri.exercise201.entity.ShippingZone;

public interface ShippingRateService {
    List<ShippingRate> findAll();

    Optional<ShippingRate> findById(UUID id);

    List<ShippingRate> findByShippingZone(ShippingZone shippingZone);

    List<ShippingRate> findByShippingZoneAndWeightUnit(ShippingZone shippingZone, ShippingRate.WeightUnit weightUnit);

    ShippingRate save(ShippingRate shippingRate);

    void deleteById(UUID id);

    void deleteByShippingZone(ShippingZone shippingZone);

    boolean existsById(UUID id);
}