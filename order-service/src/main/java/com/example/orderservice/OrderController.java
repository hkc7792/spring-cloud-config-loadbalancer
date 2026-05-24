package com.example.orderservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderServiceProperties orderServiceProperties;

    public OrderController(OrderService orderService, OrderServiceProperties orderServiceProperties) {
        this.orderService = orderService;
        this.orderServiceProperties = orderServiceProperties;
    }

    @GetMapping
    public Mono<Map<String, Object>> placeOrder() {
        // Use Mono.zip to make concurrent, non-blocking calls
        return Mono.zip(
            orderService.getProductById(1),
            orderService.getInventoryByProductId(1)
        ).map(tuple -> Map.of(
            "orderId",   UUID.randomUUID().toString(),
            "status",    "CREATED",
            "product",   tuple.getT1(), // Result from the first Mono
            "inventory", tuple.getT2()  // Result from the second Mono
        ));
    }

    @GetMapping("/test")
    public void getProps(){
        System.out.println("order-service max items : "+orderServiceProperties.getMax());
        System.out.println("order-service purchasing in currency : "+orderServiceProperties.getCurrency());
    }
}
