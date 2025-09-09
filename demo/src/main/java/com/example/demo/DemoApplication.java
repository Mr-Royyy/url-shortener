package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// Remove Redis repositories enablement to avoid bean conflicts
//@EnableRedisRepositories(basePackages = "com.example.repository.redis")
@EnableJpaRepositories(basePackages = "com.example.demo.repository.jpa")
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
