package com.academy.cacheasside.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.academy.cacheasside.AbstractIntegrationTest;
import com.academy.cacheasside.entity.Product;
import com.academy.cacheasside.exception.ProductNotFoundException;
import com.academy.cacheasside.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class ProductServiceCacheAsideTest extends AbstractIntegrationTest {

    @Autowired
    private ProductService productService;

    @MockitoSpyBean
    private ProductRepository productRepository;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        Logger serviceLogger = (Logger) LoggerFactory.getLogger(ProductService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        serviceLogger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        Logger serviceLogger = (Logger) LoggerFactory.getLogger(ProductService.class);
        serviceLogger.detachAppender(logAppender);
    }

    @Test
    void shouldHitCacheOnSecondFindById() {
        Product saved = productRepository.save(
                new Product(null, "Keyboard", "Mechanical keyboard", new BigDecimal("250.00")));

        productService.findById(saved.getId());
        productService.findById(saved.getId());

        verify(productRepository, times(1)).findById(saved.getId());

        long cacheMissLogs = logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message.contains("CACHE MISS"))
                .count();
        assertThat(cacheMissLogs).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        assertThatThrownBy(() -> productService.findById(999_999L))
                .isInstanceOf(ProductNotFoundException.class);
    }
}