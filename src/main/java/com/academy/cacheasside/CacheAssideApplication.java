package com.academy.cacheasside;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class CacheAssideApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheAssideApplication.class, args);
    }

}
