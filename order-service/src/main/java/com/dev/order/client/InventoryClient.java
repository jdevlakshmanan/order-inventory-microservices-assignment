package com.dev.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class InventoryClient {

    private final WebClient webClient;

    public InventoryClient(@Value("${inventory.service.base-url:http://localhost:8081}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public void reserve(String sku, int quantity) {
        Map<String, Object> body = new HashMap<>();
        body.put("sku", sku);
        body.put("delta", -quantity);

        webClient.post()
                .uri("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ClientResponse::createException)
                .bodyToMono(Void.class)
                .block();
    }
}
