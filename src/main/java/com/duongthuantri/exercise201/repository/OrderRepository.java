package com.duongthuantri.exercise201.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Customer;
import com.duongthuantri.exercise201.entity.Order;
import com.duongthuantri.exercise201.entity.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByCustomer(Customer customer);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus orderStatus);
}