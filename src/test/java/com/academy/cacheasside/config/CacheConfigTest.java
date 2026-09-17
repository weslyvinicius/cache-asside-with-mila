package com.academy.cacheasside.config;

import com.academy.cacheasside.AbstractIntegrationTest;
import com.academy.cacheasside.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class CacheConfigTest extends AbstractIntegrationTest {

    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager redisCacheManager;

    @Autowired
    @Qualifier("caffeineCacheManager")
    private CacheManager caffeineCacheManager;

    @Test
    void shouldRoundTripProductThroughRedisCache() {
        Cache products = redisCacheManager.getCache("products");
        assertThat(products).isNotNull();

        Product product = new Product(1L, "Keyboard", "Mechanical keyboard", new BigDecimal("250.00"));

        products.put(1L, product);
        Product cached = products.get(1L, Product.class);

        assertThat(cached).isNotNull();
        assertThat(cached.getName()).isEqualTo("Keyboard");
        assertThat(cached.getPrice()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    void shouldRoundTripValueThroughCaffeineCache() {
        Cache categories = caffeineCacheManager.getCache("categories");
        assertThat(categories).isNotNull();

        Product standIn = new Product(2L, "Electronics", "Stand-in before Category exists", new BigDecimal("0.00"));

        categories.put(2L, standIn);
        Product cached = categories.get(2L, Product.class);

        assertThat(cached).isNotNull();
        assertThat(cached.getName()).isEqualTo("Electronics");
    }
}