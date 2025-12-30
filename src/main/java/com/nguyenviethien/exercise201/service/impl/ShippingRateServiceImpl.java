package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.ShippingRate;
import com.nguyenviethien.exercise201.entity.ShippingZone;
import com.nguyenviethien.exercise201.repository.ShippingRateRepository;
import com.nguyenviethien.exercise201.service.ShippingRateService;

@Service
@Transactional
public class ShippingRateServiceImpl implements ShippingRateService {

    @Autowired
    private ShippingRateRepository shippingRateRepository;

    @Override
    public List<ShippingRate> findAll() {
        return shippingRateRepository.findAll();
    }

    @Override
    public Optional<ShippingRate> findById(UUID id) {
        return shippingRateRepository.findById(id);
    }

    @Override
    public List<ShippingRate> findByShippingZone(ShippingZone shippingZone) {
        return shippingRateRepository.findByShippingZone(shippingZone);
    }

    @Override
    public List<ShippingRate> findByShippingZoneAndWeightUnit(ShippingZone shippingZone,
            ShippingRate.WeightUnit weightUnit) {
        return shippingRateRepository.findByShippingZoneAndWeightUnit(shippingZone, weightUnit);
    }

    @Override
    public ShippingRate save(ShippingRate shippingRate) {
        return shippingRateRepository.save(shippingRate);
    }

    @Override
    public void deleteById(UUID id) {
        shippingRateRepository.deleteById(id);
    }

    @Override
    public void deleteByShippingZone(ShippingZone shippingZone) {
        shippingRateRepository.deleteByShippingZone(shippingZone);
    }

    @Override
    public boolean existsById(UUID id) {
        return shippingRateRepository.existsById(id);
    }
}