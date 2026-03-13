package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.Favorite;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.repository.FavoriteRepository;
import com.nguyenviethien.exercise201.repository.ProductRepository;
import com.nguyenviethien.exercise201.service.FavoriteService;

@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Favorite> getFavoritesByCustomerId(UUID customerId) {
        return favoriteRepository.findByCustomerId(customerId);
    }

    @Override
    public Favorite addFavorite(UUID customerId, UUID productId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (favoriteRepository.existsByCustomerAndProduct(customer, product)) {
            // Already exists - return existing
            Optional<Favorite> existing = favoriteRepository.findByCustomer(customer).stream()
                    .filter(f -> f.getProduct().getId().equals(product.getId())).findFirst();
            return existing.orElse(null);
        }

        Favorite fav = new Favorite();
        fav.setCustomer(customer);
        fav.setProduct(product);
        return favoriteRepository.save(fav);
    }

    @Override
    public void removeFavorite(UUID customerId, UUID productId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        favoriteRepository.deleteByCustomerAndProduct(customer, product);
    }

    @Override
    public boolean isFavorited(UUID customerId, UUID productId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return favoriteRepository.existsByCustomerAndProduct(customer, product);
    }
}


