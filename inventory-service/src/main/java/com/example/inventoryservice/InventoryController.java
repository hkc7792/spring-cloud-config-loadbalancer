package com.example.inventoryservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Value("${server.port}")
    private String port;

    @GetMapping
    public Map<String, Object> getAllInventory() {
        return Map.of(
            "service", "inventory-service",
            "servedByPort", port,
            "items", List.of(
                Map.of("productId", 1, "quantity", 100, "available", true),
                Map.of("productId", 2, "quantity", 50,  "available", true)
            )
        );
    }

    @GetMapping("/{productId}")
    public Map<String, Object> getInventoryByProduct(@PathVariable int productId) {
        return Map.of(
            "productId", productId,
            "quantity", 100,
            "available", true,
            "servedByPort", port
        );
    }
}
