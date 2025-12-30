package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.CustomerAddress;
import com.nguyenviethien.exercise201.repository.CustomerAddressRepository;
import com.nguyenviethien.exercise201.service.CustomerAddressService;

@Service
@Transactional
public class CustomerAddressServiceImpl implements CustomerAddressService {

    @Autowired
    private CustomerAddressRepository customerAddressRepository;

    @Override
    public List<CustomerAddress> findAll() {
        return customerAddressRepository.findAll();
    }

    @Override
    public Optional<CustomerAddress> findById(UUID id) {
        return customerAddressRepository.findById(id);
    }

    @Override
    public List<CustomerAddress> findByCustomer(Customer customer) {
        return customerAddressRepository.findByCustomer(customer);
    }

    @Override
    public CustomerAddress save(CustomerAddress address) {
        return customerAddressRepository.save(address);
    }

    @Override
    public void deleteById(UUID id) {
        customerAddressRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return customerAddressRepository.existsById(id);
    }
}