package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Card;
import com.duongthuantri.exercise201.entity.CardItem;
import com.duongthuantri.exercise201.entity.Customer;

@Repository
@RepositoryRestResource(path = "cards")
public interface CardRepository extends JpaRepository<Card, UUID> {
    Optional<Card> findByCustomerId(UUID customerId);

}