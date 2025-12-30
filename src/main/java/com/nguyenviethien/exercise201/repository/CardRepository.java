// package com.nguyenviethien.exercise201.repository;

// import java.util.UUID;
// import java.util.List;
// import java.util.Optional;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.rest.core.annotation.RepositoryRestResource;
// import org.springframework.stereotype.Repository;

// import com.nguyenviethien.exercise201.entity.Card;
// import com.nguyenviethien.exercise201.entity.CardItem;
// import com.nguyenviethien.exercise201.entity.Customer;

// @Repository
// @RepositoryRestResource(path = "cards")
// public interface CardRepository extends JpaRepository<Card, UUID> {
//     Optional<Card> findByCustomerId(UUID customerId);

//     Optional<Card> findByCustomer(Customer customer); // Add if missing

//     List<Card> findAllByCustomer(Customer customer);

//     void deleteAllByCustomer(Customer customer);

// }



package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Card;
import com.nguyenviethien.exercise201.entity.Customer;

@Repository
@RepositoryRestResource(path = "cards")
public interface CardRepository extends JpaRepository<Card, UUID> {
    
    Optional<Card> findByCustomerId(UUID customerId);
    Optional<Card> findByCustomer(Customer customer);
    
    List<Card> findAllByCustomer(Customer customer);
    
    void deleteAllByCustomer(Customer customer);
    
    // Enhanced queries
    @Query("SELECT c FROM Card c WHERE c.customer.id = :customerId")
    Optional<Card> findByCustomerIdWithItems(@Param("customerId") UUID customerId);
    
    @Query("SELECT COUNT(ci) FROM Card c JOIN c.cardItems ci WHERE c.customer.id = :customerId")
    int countItemsByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT SUM(ci.quantity) FROM Card c JOIN c.cardItems ci WHERE c.customer.id = :customerId")
    Integer sumQuantityByCustomerId(@Param("customerId") UUID customerId);
    
    // Check if customer has active cart
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Card c WHERE c.customer.id = :customerId")
    boolean existsByCustomerId(@Param("customerId") UUID customerId);
}
