package com.assignment.asm.repository;

import com.assignment.asm.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = "SELECT * FROM products WHERE active = true", nativeQuery = true)
    List<Product> findAllActive();
}
