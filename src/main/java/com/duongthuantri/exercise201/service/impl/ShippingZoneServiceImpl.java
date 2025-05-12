package com.duongthuantri.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duongthuantri.exercise201.entity.ShippingZone;
import com.duongthuantri.exercise201.repository.ShippingZoneRepository;
import com.duongthuantri.exercise201.service.ShippingZoneService;

@Service
@Transactional
public class ShippingZoneServiceImpl implements ShippingZoneService {

    @Autowired
    private ShippingZoneRepository shippingZoneRepository;

    @Override
    public List<ShippingZone> findAll() {
        return shippingZoneRepository.findAll();
    }

    @Override
    public Optional<ShippingZone> findById(UUID id) {
        return shippingZoneRepository.findById(id);
    }

    @Override
    public List<ShippingZone> findByActiveTrue() {
        return shippingZoneRepository.findByActiveTrue();
    }

    @Override
    public List<ShippingZone> findByFreeShippingTrue() {
        return shippingZoneRepository.findByFreeShippingTrue();
    }

    @Override
    public ShippingZone save(ShippingZone shippingZone) {
        return shippingZoneRepository.save(shippingZone);
    }

    @Override
    public void deleteById(UUID id) {
        shippingZoneRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return shippingZoneRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return shippingZoneRepository.existsByName(name);
    }
}