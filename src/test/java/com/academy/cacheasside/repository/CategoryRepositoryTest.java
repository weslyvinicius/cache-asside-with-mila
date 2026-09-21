package com.academy.cacheasside.repository;

import com.academy.cacheasside.AbstractIntegrationTest;
import com.academy.cacheasside.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class CategoryRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSaveAndFindCategory() {
        Category category = new Category(
                null,
                "Electronics",
                "Electronic products"
        );

        Category saved = categoryRepository.save(category);

        Category found = categoryRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Electronics");
        assertThat(found.getDescription()).isEqualTo("Electronic products");
    }
}