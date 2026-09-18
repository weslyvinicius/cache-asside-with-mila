package com.academy.cacheasside.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.academy.cacheasside.AbstractIntegrationTest;
import com.academy.cacheasside.entity.Category;
import com.academy.cacheasside.repository.CategoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class CategoryServiceCacheAsideTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoSpyBean
    private CategoryRepository categoryRepository;

    @Autowired
    private CacheManager caffeineCacheManager;

    private Logger serviceLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        Cache categories = caffeineCacheManager.getCache("categories");
        assertThat(categories).isNotNull();
        categories.clear();

        serviceLogger = (Logger) LoggerFactory.getLogger(CategoryService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        serviceLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        serviceLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void shouldFetchFromDatabaseOnCacheMissAndThenFromCache() {
        Category saved = categoryRepository.save(
                new Category(
                        null,
                        "Electronics",
                        "Electronic products"
                )
        );

        Category firstCall = categoryService.findById(saved.getId());
        Category secondCall = categoryService.findById(saved.getId());

        assertThat(firstCall.getName()).isEqualTo("Electronics");
        assertThat(secondCall.getName()).isEqualTo("Electronics");

        verify(categoryRepository, times(1)).findById(saved.getId());

        long cacheMissLogs = logAppender.list.stream()
                .filter(event -> event.getFormattedMessage()
                        .contains("[CAFFEINE] CACHE MISS - fetching category"))
                .count();

        assertThat(cacheMissLogs).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> categoryService.findById(999_999L)
        ).isInstanceOf(com.academy.cacheasside.exception.CategoryNotFoundException.class);
    }
}