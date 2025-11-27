package com.dev.order.controller;

import com.dev.order.dto.OrderRequest;
import com.dev.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/order")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody @Valid OrderRequest request) {
        Long orderId = orderService.placeOrder(request);
        return ResponseEntity.ok("Order placed and order ID is: " + orderId);
    }
}
