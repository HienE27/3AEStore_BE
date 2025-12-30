package com.nguyenviethien.exercise201.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.entity.Country;
import com.nguyenviethien.exercise201.service.CountryService;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    @Autowired
    private CountryService countryService;

    @GetMapping
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(countryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Country> getCountryById(@PathVariable Integer id) {
        return countryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/iso/{iso}")
    public ResponseEntity<Country> getCountryByIso(@PathVariable String iso) {
        return countryService.findByIso(iso)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/iso3/{iso3}")
    public ResponseEntity<Country> getCountryByIso3(@PathVariable String iso3) {
        return countryService.findByIso3(iso3)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Country> createCountry(@RequestBody Country country) {
        return ResponseEntity.ok(countryService.save(country));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Country> updateCountry(
            @PathVariable Integer id,
            @RequestBody Country country) {
        if (!countryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        country.setId(id);
        return ResponseEntity.ok(countryService.save(country));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable Integer id) {
        if (!countryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        countryService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}