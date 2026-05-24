package com.example.orderservice;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OrderService {

    private final WebClient.Builder webClientBuilder;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    /**
     * Constructor to inject dependencies and programmatically configure resilience.
     * We fetch the circuit breaker and retry instances from their respective registries,
     * which are pre-configured from application.yml.
     */
    public OrderService(WebClient.Builder webClientBuilder,
                        CircuitBreakerRegistry circuitBreakerRegistry,
                        RetryRegistry retryRegistry) {
        this.webClientBuilder = webClientBuilder;
        // Fetch the configured CircuitBreaker instance by the name we gave it in application.yml
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("external-service");
        // Fetch the configured Retry instance
        this.retry = retryRegistry.retry("external-service");
    }

    /**
     * Fetches product details using a reactive, programmatic approach to resilience.
     * The resilience logic is applied directly to the Mono stream.
     */
    public Mono<Map> getProductById(int productId) {
        return webClientBuilder.build()
                .get()
                .uri("http://product-service/api/products/{productId}", productId)
                .retrieve()
                .bodyToMono(Map.class)
                // --- Reactive Resilience ---
                // 1. Apply the Retry logic first.
                .transform(RetryOperator.of(retry))
                // 2. Then, apply the Circuit Breaker.
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                // 3. Define a reactive fallback using onErrorResume.
                // This is the equivalent of 'fallbackMethod' but fits into the reactive chain.
                .onErrorResume(throwable -> {
                    // Log the error for monitoring
                    System.err.println("Reactive fallback for product service triggered. Error: " + throwable.getMessage());
                    // Return a default Mono as the fallback response
                    return Mono.just(Map.of(
                            "id", productId,
                            "name", "Default Product",
                            "price", 0.00,
                            "error", "Product service is currently unavailable."
                    ));
                });
    }

    /**
     * Fetches inventory details using the same reactive resilience pattern.
     */
    public Mono<Map> getInventoryByProductId(int productId) {
        return webClientBuilder.build()
                .get()
                .uri("http://inventory-service/api/inventory/{productId}", productId)
                .retrieve()
                .bodyToMono(Map.class)
                // Apply the same resilience operators to this call
                .transform(RetryOperator.of(retry))
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                // Provide a specific fallback for inventory
                .onErrorResume(throwable -> {
                    System.err.println("Reactive fallback for inventory service triggered. Error: " + throwable.getMessage());
                    return Mono.just(Map.of(
                            "productId", productId,
                            "quantity", 0,
                            "available", false,
                            "error", "Inventory service is currently unavailable."
                    ));
                });
    }
}
