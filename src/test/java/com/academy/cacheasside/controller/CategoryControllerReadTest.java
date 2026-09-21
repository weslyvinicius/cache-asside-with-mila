package com.academy.cacheasside.controller;

import com.academy.cacheasside.AbstractIntegrationTest;
import com.academy.cacheasside.entity.Category;
import com.academy.cacheasside.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
class CategoryControllerReadTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldReturnCategoryWhenExists() throws Exception {
        Category saved = categoryRepository.save(
                new Category(
                        null,
                        "Electronics",
                        "Electronic products"
                )
        );

        mockMvc.perform(get("/api/categories/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Electronic products"));
    }

    @Test
    void shouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}