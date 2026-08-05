package com.academy.cacheasside.repository;

import com.academy.cacheasside.AbstractIntegrationTest;
import com.academy.cacheasside.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class ProductRepositoryTest extends AbstractIntegrationTest {

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