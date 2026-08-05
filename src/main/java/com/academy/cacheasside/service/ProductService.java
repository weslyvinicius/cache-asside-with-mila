package com.academy.cacheasside.service;

import com.academy.cacheasside.dto.ProductRequest;
import com.academy.cacheasside.entity.Product;
import com.academy.cacheasside.exception.ProductNotFoundException;
import com.academy.cacheasside.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable(cacheNames = "products", cacheManager = "redisCacheManager")
    public Product findById(Long id) {
        log.info("[REDIS] CACHE MISS - fetching product {} from DB", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @CachePut(cacheNames = "products", cacheManager = "redisCacheManager", key = "#result.id")
    public Product create(ProductRequest request) {
        Product product = new Product(null, request.name(), request.description(), request.price());
        return productRepository.save(product);
    }

    @CachePut(cacheNames = "products", cacheManager = "redisCacheManager", key = "#id")
    public Product update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        return productRepository.save(product);
    }

    @CacheEvict(cacheNames = "products", cacheManager = "redisCacheManager", key = "#id")
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }
}