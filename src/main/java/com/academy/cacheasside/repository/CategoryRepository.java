package com.academy.cacheasside.repository;

import com.academy.cacheasside.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

