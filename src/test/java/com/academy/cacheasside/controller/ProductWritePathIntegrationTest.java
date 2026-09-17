package com.academy.cacheasside.controller;

import com.academy.cacheasside.AbstractIntegrationTest;
import com.academy.cacheasside.entity.Product;
import com.academy.cacheasside.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
class ProductWritePathIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldCreateProduct() throws Exception {
        String request = """
                {
                    "name": "Keyboard",
                    "description": "Mechanical keyboard",
                    "price": 250.00
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.description").value("Mechanical keyboard"))
                .andExpect(jsonPath("$.price").value(250.00));
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        String request = """
                {
                    "name": "",
                    "description": "Mechanical keyboard",
                    "price": 250.00
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        Product saved = productRepository.save(
                new Product(
                        null,
                        "Keyboard",
                        "Mechanical keyboard",
                        new BigDecimal("250.00")
                )
        );

        String request = """
                {
                    "name": "Updated Keyboard",
                    "description": "Updated description",
                    "price": 300.00
                }
                """;

        mockMvc.perform(put("/api/products/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Keyboard"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.price").value(300.00));

        Product updated = productRepository.findById(saved.getId()).orElseThrow();

        org.assertj.core.api.Assertions.assertThat(updated.getName())
                .isEqualTo("Updated Keyboard");
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        Product saved = productRepository.save(
                new Product(
                        null,
                        "Keyboard",
                        "Mechanical keyboard",
                        new BigDecimal("250.00")
                )
        );

        mockMvc.perform(delete("/api/products/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(
                productRepository.findById(saved.getId())
        ).isEmpty();
    }
}