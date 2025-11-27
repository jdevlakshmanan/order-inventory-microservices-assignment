package com.dev.order.service.impl;

import com.dev.order.client.InventoryClient;
import com.dev.order.domain.OrderEntity;
import com.dev.order.dto.OrderRequest;
import com.dev.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderServiceImplTest {

    private InventoryClient inventoryClient;
    private OrderRepository repo;
    private OrderServiceImpl service;

    @BeforeEach
    void setup() {

        inventoryClient = mock(InventoryClient.class);
        repo = mock(OrderRepository.class);
        service = new OrderServiceImpl(inventoryClient, repo);
    }

    @Test
    void placeOrder_reservesAndSaves() {

        OrderRequest.OrderLine l = OrderRequest.OrderLine.builder().sku("sku-x").quantity(2).build();
        OrderRequest req = OrderRequest.builder().customerId("c1").items(List.of(l)).build();
        when(repo.save(any(OrderEntity.class))).thenAnswer(i -> {
            OrderEntity o = i.getArgument(0);
            o.setId(5L);
            return o;
        });

        Long id = service.placeOrder(req);
        assertEquals(5L, id);
        verify(inventoryClient).reserve("sku-x", 2);
        verify(repo).save(any());
    }

    @Test
    void placeOrder_multipleItems_reservesAllAndSaves() {

        OrderRequest.OrderLine l1 = OrderRequest.OrderLine.builder().sku("sku-a").quantity(1).build();
        OrderRequest.OrderLine l2 = OrderRequest.OrderLine.builder().sku("sku-b").quantity(3).build();
        OrderRequest req = OrderRequest.builder().customerId("c2").items(List.of(l1, l2)).build();

        // capture saved entity to assert contents
        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        when(repo.save(captor.capture())).thenAnswer(i -> {
            OrderEntity o = i.getArgument(0);
            o.setId(10L);
            return o;
        });

        Long id = service.placeOrder(req);
        assertEquals(10L, id);

        // verify inventory reservations called for both items
        verify(inventoryClient).reserve("sku-a", 1);
        verify(inventoryClient).reserve("sku-b", 3);

        // verify saved order contains expected values
        OrderEntity saved = captor.getValue();
        assertNotNull(saved);
        assertEquals("c2", saved.getCustomerId());
        assertEquals("CONFIRMED", saved.getStatus());
        assertEquals(2, saved.getItems().size());
    }

    @Test
    void placeOrder_inventoryReserveFails_throwsAndDoesNotSave() {

        OrderRequest.OrderLine l1 = OrderRequest.OrderLine.builder().sku("sku-fail").quantity(4).build();
        OrderRequest req = OrderRequest.builder().customerId("c3").items(List.of(l1)).build();

        doThrow(new RuntimeException("reserve failed")).when(inventoryClient).reserve("sku-fail", 4);

        assertThrows(RuntimeException.class, () -> service.placeOrder(req));

        // verify save was never called due to reservation failure
        verify(repo, never()).save(any());
    }

    @Test
    void placeOrder_zeroQuantity_reservesZeroAndSaves() {

        OrderRequest.OrderLine l = OrderRequest.OrderLine.builder().sku("sku-zero").quantity(0).build();
        OrderRequest req = OrderRequest.builder().customerId("c4").items(List.of(l)).build();

        when(repo.save(any(OrderEntity.class))).thenAnswer(i -> {
            OrderEntity o = i.getArgument(0);
            o.setId(20L);
            return o;
        });

        Long id = service.placeOrder(req);
        assertEquals(20L, id);
        verify(inventoryClient).reserve("sku-zero", 0);
        verify(repo).save(any());
    }

    @Test
    void placeOrder_nullItems_throwsNPE() {

        OrderRequest req = OrderRequest.builder().customerId("c5").items(null).build();
        assertThrows(NullPointerException.class, () -> service.placeOrder(req));
    }

    @Test
    void placeOrder_repoSaveThrows_exceptionPropagatesAndReservationsOccur() {

        OrderRequest.OrderLine l = OrderRequest.OrderLine.builder().sku("sku-x").quantity(1).build();
        OrderRequest req = OrderRequest.builder().customerId("c6").items(List.of(l)).build();

        doThrow(new RuntimeException("db error")).when(repo).save(any(OrderEntity.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.placeOrder(req));
        assertEquals("db error", ex.getMessage());

        // reservation should have been attempted before save
        verify(inventoryClient).reserve("sku-x", 1);
        verify(repo).save(any());
    }
}
