package com.nguyenviethien.exercise201.repository;

import com.nguyenviethien.exercise201.entity.Order;
import com.nguyenviethien.exercise201.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@RepositoryRestResource(path = "payments")
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
