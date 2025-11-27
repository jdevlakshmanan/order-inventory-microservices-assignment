package com.dev.inventory.strategy;

import com.dev.inventory.domain.InventoryBatch;
import com.dev.inventory.dto.InventoryUpdateRequest;
import com.dev.inventory.repository.InventoryBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SimpleInventoryStrategyTest {

    private InventoryBatchRepository repo;
    private SimpleInventoryStrategy strategy;

    @BeforeEach
    void setup() {

        repo = mock(InventoryBatchRepository.class);
        strategy = new SimpleInventoryStrategy(repo);
    }

    @Test
    void addCreatesNewBatch() {

        InventoryUpdateRequest r = new InventoryUpdateRequest("sku-a", 10, null);
        strategy.applyUpdate(r);
        verify(repo).save(any(InventoryBatch.class));
    }

    @Test
    void consumeReducesBatches_orThrows() {

        InventoryUpdateRequest r = new InventoryUpdateRequest("sku-b", -5, null);
        InventoryBatch b1 = InventoryBatch.builder().id(1L).sku("sku-b").quantity(3).expiryDate(LocalDate.now().plusDays(1)).build();
        InventoryBatch b2 = InventoryBatch.builder().id(2L).sku("sku-b").quantity(3).expiryDate(LocalDate.now().plusDays(2)).build();
        List<InventoryBatch> batches = new ArrayList<>();
        batches.add(b1); batches.add(b2);
        when(repo.findBySkuOrderByExpiryDateAsc("sku-b")).thenReturn(batches);
        strategy.applyUpdate(r);
        ArgumentCaptor<InventoryBatch> cap = ArgumentCaptor.forClass(InventoryBatch.class);
        verify(repo, atLeastOnce()).save(cap.capture());
        assertTrue(cap.getAllValues().stream().anyMatch(x -> x.getQuantity() < 3));
    }
}
