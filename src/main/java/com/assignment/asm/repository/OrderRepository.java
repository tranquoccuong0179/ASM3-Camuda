package com.assignment.asm.repository;

import com.assignment.asm.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByBusinessKey(String businessKey);
}
