package com.dev.order.service.impl;

import com.dev.order.client.InventoryClient;
import com.dev.order.dto.OrderRequest;
import com.dev.order.domain.OrderEntity;
import com.dev.order.domain.OrderLineEntity;
import com.dev.order.repository.OrderRepository;
import com.dev.order.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final InventoryClient inventoryClient;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(InventoryClient inventoryClient, OrderRepository orderRepository) {
        this.inventoryClient = inventoryClient;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public Long placeOrder(OrderRequest request) {

        // Reserve inventory first
        request.getItems().forEach(ol -> inventoryClient.reserve(ol.getSku(), ol.getQuantity()));

        // Persist order
        OrderEntity order = OrderEntity.builder()
                .customerId(request.getCustomerId())
                .status("CONFIRMED")
                .items(request.getItems().stream().map(ol -> OrderLineEntity.builder().sku(ol.getSku()).quantity(ol.getQuantity()).build()).collect(Collectors.toList()))
                .build();

        OrderEntity saved = orderRepository.save(order);
        return saved.getId();
    }
}
