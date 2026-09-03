package com.zestindia.productapi.repository;

import com.zestindia.productapi.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	Page<Product> findByProductNameContainingIgnoreCase(String name, Pageable pageable);
}
