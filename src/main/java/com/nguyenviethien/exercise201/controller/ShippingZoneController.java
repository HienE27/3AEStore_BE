package com.nguyenviethien.exercise201.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.entity.ShippingRate;
import com.nguyenviethien.exercise201.entity.ShippingZone;
import com.nguyenviethien.exercise201.service.ShippingRateService;
import com.nguyenviethien.exercise201.service.ShippingZoneService;

@RestController
@RequestMapping("/api/shipping-zones")
public class ShippingZoneController {

    @Autowired
    private ShippingZoneService shippingZoneService;

    @Autowired
    private ShippingRateService shippingRateService;

    @GetMapping
    public ResponseEntity<List<ShippingZone>> getAllShippingZones() {
        return ResponseEntity.ok(shippingZoneService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShippingZone> getShippingZoneById(@PathVariable UUID id) {
        return shippingZoneService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ShippingZone>> getActiveShippingZones() {
        return ResponseEntity.ok(shippingZoneService.findByActiveTrue());
    }

    @GetMapping("/free-shipping")
    public ResponseEntity<List<ShippingZone>> getFreeShippingZones() {
        return ResponseEntity.ok(shippingZoneService.findByFreeShippingTrue());
    }

    @PostMapping
    public ResponseEntity<?> createShippingZone(@RequestBody ShippingZone shippingZone) {
        if (shippingZoneService.existsByName(shippingZone.getName())) {
            return ResponseEntity.badRequest()
                    .body("Shipping zone with this name already exists");
        }
        return ResponseEntity.ok(shippingZoneService.save(shippingZone));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateShippingZone(
            @PathVariable UUID id,
            @RequestBody ShippingZone shippingZone) {
        if (!shippingZoneService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!shippingZone.getName().equals(shippingZoneService.findById(id).get().getName()) &&
                shippingZoneService.existsByName(shippingZone.getName())) {
            return ResponseEntity.badRequest()
                    .body("Shipping zone with this name already exists");
        }
        shippingZone.setId(id);
        return ResponseEntity.ok(shippingZoneService.save(shippingZone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShippingZone(@PathVariable UUID id) {
        if (!shippingZoneService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        shippingZoneService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/rates")
    public ResponseEntity<List<ShippingRate>> getShippingZoneRates(@PathVariable UUID id) {
        return shippingZoneService.findById(id)
                .map(zone -> ResponseEntity.ok(shippingRateService.findByShippingZone(zone)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/rates")
    public ResponseEntity<ShippingRate> addShippingRate(
            @PathVariable UUID id,
            @RequestBody ShippingRate shippingRate) {
        return shippingZoneService.findById(id)
                .map(zone -> {
                    shippingRate.setShippingZone(zone);
                    return ResponseEntity.ok(shippingRateService.save(shippingRate));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}