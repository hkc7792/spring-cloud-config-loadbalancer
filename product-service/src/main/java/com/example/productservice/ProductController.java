package com.example.productservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Value("${server.port}")
    private String port;

    @GetMapping
    public Map<String, Object> getAllProducts() {
        return Map.of(
            "service", "product-service",
            "servedByPort", port,
            "products", List.of(
                Map.of("id", 1, "name", "Laptop", "price", 999.99),
                Map.of("id", 2, "name", "Phone",  "price", 499.99)
            )
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getProduct(@PathVariable int id) {
        return Map.of(
            "id", id,
            "name", "Laptop",
            "price", 999.99,
            "servedByPort", port
        );
    }
}
