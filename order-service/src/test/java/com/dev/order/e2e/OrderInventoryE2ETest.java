package com.dev.order.e2e;

import com.dev.order.OrderApplication;
import com.dev.order.dto.OrderRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public class OrderInventoryE2ETest {

    private static ConfigurableApplicationContext inventoryCtx;
    private static ConfigurableApplicationContext orderCtx;
    private static final TestRestTemplate restTemplate = new TestRestTemplate();

    @BeforeAll
    static void startBoth() {

        try {
            Class<?> invClass = Class.forName("com.dev.inventory.InventoryApplication");
            inventoryCtx = SpringApplication.run(invClass, "--server.port=8085");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("InventoryApplication class not found on test classpath", e);
        }

        orderCtx = SpringApplication.run(OrderApplication.class, "--server.port=8086", "--inventory.service.base-url=http://localhost:8085");
    }

    @AfterAll
    static void stopBoth() {

        if (orderCtx != null) orderCtx.close();
        if (inventoryCtx != null) inventoryCtx.close();
    }

    @Test
    void placeOrder_roundtrip() {

        // add inventory via inventory service
        String inventoryUrl = "http://localhost:8081/inventory/update";
        Map<String, Object> add = Map.of("sku","E2E-SKU","delta",5);
        ResponseEntity<String> addResp = restTemplate.postForEntity(inventoryUrl, add, String.class);
        Assertions.assertTrue(addResp.getStatusCode().is2xxSuccessful());

        // place order via order service
        String orderUrl = "http://localhost:8080/order";
        OrderRequest.OrderLine line = OrderRequest.OrderLine.builder().sku("E2E-SKU").quantity(2).build();
        OrderRequest req = OrderRequest.builder().customerId("E2E").items(List.of(line)).build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderRequest> entity = new HttpEntity<>(req, headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(orderUrl, entity, String.class);
        Assertions.assertTrue(resp.getStatusCode().is2xxSuccessful());
    }
}
