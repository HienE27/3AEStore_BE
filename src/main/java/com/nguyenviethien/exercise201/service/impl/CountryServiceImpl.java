package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Country;
import com.nguyenviethien.exercise201.repository.CountryRepository;
import com.nguyenviethien.exercise201.service.CountryService;

@Service
@Transactional
public class CountryServiceImpl implements CountryService {

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public List<Country> findAll() {
        return countryRepository.findAll();
    }

    @Override
    public Optional<Country> findById(Integer id) {
        return countryRepository.findById(id);
    }

    @Override
    public Optional<Country> findByIso(String iso) {
        return countryRepository.findByIso(iso);
    }

    @Override
    public Optional<Country> findByIso3(String iso3) {
        return countryRepository.findByIso3(iso3);
    }

    @Override
    public Country save(Country country) {
        return countryRepository.save(country);
    }

    @Override
    public void deleteById(Integer id) {
        countryRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return countryRepository.existsById(id);
    }
}