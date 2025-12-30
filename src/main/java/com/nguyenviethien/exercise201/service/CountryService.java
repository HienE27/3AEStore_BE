package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;

import com.nguyenviethien.exercise201.entity.Country;

public interface CountryService {
    List<Country> findAll();

    Optional<Country> findById(Integer id);

    Optional<Country> findByIso(String iso);

    Optional<Country> findByIso3(String iso3);

    Country save(Country country);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}