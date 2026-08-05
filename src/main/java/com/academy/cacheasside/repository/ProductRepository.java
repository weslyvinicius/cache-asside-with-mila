package com.academy.cacheasside.repository;

import com.academy.cacheasside.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}