package com.academy.cacheasside.service;

import com.academy.cacheasside.dto.CategoryRequest;
import com.academy.cacheasside.entity.Category;
import com.academy.cacheasside.exception.CategoryNotFoundException;
import com.academy.cacheasside.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(
            cacheNames = "categories",
            cacheManager = "caffeineCacheManager",
            key = "#id"
    )
    public Category findById(Long id) {
        log.info("[CAFFEINE] CACHE MISS - fetching category {} from DB", id);

        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @CachePut(
            cacheNames = "categories",
            cacheManager = "caffeineCacheManager",
            key = "#result.id"
    )
    public Category create(CategoryRequest request) {
        Category category = new Category(
                null,
                request.name(),
                request.description()
        );

        return categoryRepository.save(category);
    }

    @CachePut(
            cacheNames = "categories",
            cacheManager = "caffeineCacheManager",
            key = "#id"
    )
    public Category update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        category.setName(request.name());
        category.setDescription(request.description());

        return categoryRepository.save(category);
    }

    @CacheEvict(
            cacheNames = "categories",
            cacheManager = "caffeineCacheManager",
            key = "#id"
    )
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}