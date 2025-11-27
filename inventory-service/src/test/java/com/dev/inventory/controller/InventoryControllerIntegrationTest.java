package com.dev.inventory.controller;

import com.dev.inventory.domain.InventoryBatch;
import com.dev.inventory.repository.InventoryBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryControllerIntegrationTest {

    @LocalServerPort
    int port;

    WebTestClient client;

    @Autowired
    InventoryBatchRepository repo;

    @BeforeEach
    void setup() {

        repo.deleteAll();
        repo.save(InventoryBatch.builder().sku("sku-int").quantity(10).expiryDate(LocalDate.now().plusDays(10)).build());
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void getBatches_returnsList() {

        client.get()
                .uri("/inventory/sku-int")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryBatch.class)
                .value(list -> assertTrue(list.size() >= 1));
    }

    @Test
    void updateEndpoint_consumesAndCreates() {

        Map<String,Object> body = Map.of("sku","sku-int","delta",-5);
        client.post()
                .uri("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isOk();
    }
}
