package com.academy.cacheasside.repository;

import com.academy.cacheasside.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Testcontainers
class ProductRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndFindProductById() {
        Product saved = productRepository.save(
                new Product(null, "Keyboard", "Mechanical keyboard", new BigDecimal("250.00")));

        Product found = productRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Keyboard");
        assertThat(found.getPrice()).isEqualByComparingTo(new BigDecimal("250.00"));
    }
}