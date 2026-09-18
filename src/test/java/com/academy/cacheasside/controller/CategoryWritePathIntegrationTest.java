package com.academy.cacheasside.controller;

import com.academy.cacheasside.AbstractIntegrationTest;
import com.academy.cacheasside.entity.Category;
import com.academy.cacheasside.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
class CategoryWritePathIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldCreateCategory() throws Exception {
        String request = """
                {
                    "name": "Electronics",
                    "description": "Electronic products"
                }
                """;

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Electronic products"));
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        String request = """
                {
                    "name": "",
                    "description": "Electronic products"
                }
                """;

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        Category saved = categoryRepository.save(
                new Category(
                        null,
                        "Electronics",
                        "Electronic products"
                )
        );

        String request = """
                {
                    "name": "Updated Electronics",
                    "description": "Updated description"
                }
                """;

        mockMvc.perform(put("/api/categories/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Electronics"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        Category updated = categoryRepository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getName()).isEqualTo("Updated Electronics");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        Category saved = categoryRepository.save(
                new Category(
                        null,
                        "Electronics",
                        "Electronic products"
                )
        );

        mockMvc.perform(delete("/api/categories/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(categoryRepository.findById(saved.getId())).isEmpty();
    }
}