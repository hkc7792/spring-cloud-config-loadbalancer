package com.example.orderservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final WebClient.Builder webClientBuilder;

    @Autowired
    private OrderServiceProperties orderServiceProperties;

    public OrderController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping
    public Map<String, Object> placeOrder() {

        // Spring Cloud Load Balancer resolves "product-service"
        // to one of its registered instances in Eureka
        Map<?, ?> product = webClientBuilder.build()
                .get()
                .uri("http://product-service/api/products/1")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<?, ?> inventory = webClientBuilder.build()
                .get()
                .uri("http://inventory-service/api/inventory/1")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return Map.of(
            "orderId",   UUID.randomUUID().toString(),
            "status",    "CREATED",
            "product",   product,
            "inventory", inventory
        );
    }

    @GetMapping("/test")
    public void getProps(){
        System.out.println("order-service max items : "+orderServiceProperties.getMax());
        System.out.println("order-service purchasing in currency : "+orderServiceProperties.getCurrency());

    }
}
