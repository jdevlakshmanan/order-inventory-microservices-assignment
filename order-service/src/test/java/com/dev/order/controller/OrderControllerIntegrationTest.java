package com.dev.order.controller;

import com.dev.order.client.InventoryClient;
import com.dev.order.dto.OrderRequest;
import com.dev.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(OrderControllerIntegrationTest.TestConfig.class)
class OrderControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    OrderRepository repo;

    @Autowired
    InventoryClient inventoryClient;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public InventoryClient inventoryClient() {
            return mock(InventoryClient.class);
        }
    }

    @BeforeEach
    void setup() {

        repo.deleteAll();
        doNothing().when(inventoryClient).reserve(anyString(), anyInt());
    }

    @Test
    void placeOrder_createsOrder_and_reservesInventory() {

        OrderRequest.OrderLine line = OrderRequest.OrderLine.builder().sku("sku-int").quantity(2).build();
        OrderRequest req = OrderRequest.builder().customerId("cust-1").items(List.of(line)).build();

        ResponseEntity<String> resp = rest.postForEntity("http://localhost:" + port + "/order", req, String.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertFalse(repo.findAll().isEmpty());
        verify(inventoryClient).reserve("sku-int", 2);
    }
}
