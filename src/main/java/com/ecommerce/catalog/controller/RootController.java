package com.ecommerce.catalog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("service", "Catalog Service");
        apiInfo.put("version", "1.0.0");
        apiInfo.put("status", "running");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("health", "/actuator/health");
        endpoints.put("categories", "/api/categories");
        endpoints.put("products", "/api/products");

        apiInfo.put("endpoints", endpoints);
        apiInfo.put("documentation", "See README.md for API documentation");

        return ResponseEntity.ok(apiInfo);
    }
}

