package com.dev.order.service;

import com.dev.order.dto.OrderRequest;

public interface OrderService {

    Long placeOrder(OrderRequest request);
}

