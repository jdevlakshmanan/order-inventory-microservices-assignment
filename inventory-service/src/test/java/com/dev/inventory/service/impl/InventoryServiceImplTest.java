package com.dev.inventory.service.impl;

import com.dev.inventory.dto.InventoryBatchDto;
import com.dev.inventory.dto.InventoryUpdateRequest;
import com.dev.inventory.domain.InventoryBatch;
import com.dev.inventory.factory.InventoryStrategyFactory;
import com.dev.inventory.repository.InventoryBatchRepository;
import com.dev.inventory.strategy.InventoryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceImplTest {

    private InventoryBatchRepository repo;
    private InventoryStrategyFactory factory;
    private InventoryServiceImpl service;

    @BeforeEach
    void setup() {

        repo = mock(InventoryBatchRepository.class);
        InventoryStrategy strat = mock(InventoryStrategy.class);
        when(strat.getName()).thenReturn("simple");
        factory = mock(InventoryStrategyFactory.class);
        when(factory.get(anyString())).thenReturn(strat);
        service = new InventoryServiceImpl(repo, factory);
    }

    @Test
    void getBatches_transformsEntitiesToDto() {

        InventoryBatch b = InventoryBatch.builder().id(1L).sku("sku-1").quantity(5).expiryDate(LocalDate.now()).build();
        when(repo.findBySkuOrderByExpiryDateAsc("sku-1")).thenReturn(List.of(b));

        List<InventoryBatchDto> list = service.getBatchesBySkuSortedByExpiry("sku-1");
        assertEquals(1, list.size());
        assertEquals("sku-1", list.get(0).getSku());
    }

    @Test
    void updateInventory_delegatesToStrategy() {

        InventoryUpdateRequest req = new InventoryUpdateRequest("sku-2", -3, "simple");
        service.updateInventory(req);
        verify(factory).get("simple");
    }

    @Test
    void getBatchesSortedByExpiry() {

        InventoryBatch b1 = InventoryBatch.builder().id(1L).sku("SKU1").quantity(10).expiryDate(LocalDate.now().plusDays(10)).build();
        InventoryBatch b2 = InventoryBatch.builder().id(2L).sku("SKU1").quantity(5).expiryDate(LocalDate.now().plusDays(5)).build();
        when(repo.findBySkuOrderByExpiryDateAsc("SKU1")).thenReturn(Arrays.asList(b2, b1));

        var dtos = service.getBatchesBySkuSortedByExpiry("SKU1");
        assertEquals(2, dtos.size());
        assertEquals(b2.getId(), dtos.get(0).getId());
    }
}
