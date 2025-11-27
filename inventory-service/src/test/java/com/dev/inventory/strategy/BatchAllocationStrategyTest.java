package com.dev.inventory.strategy;

import com.dev.inventory.domain.InventoryBatch;
import com.dev.inventory.dto.InventoryUpdateRequest;
import com.dev.inventory.repository.InventoryBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BatchAllocationStrategyTest {

    private InventoryBatchRepository repo;
    private BatchAllocationStrategy strategy;

    @BeforeEach
    void setup() {
        repo = mock(InventoryBatchRepository.class);
        strategy = new BatchAllocationStrategy(repo);
    }

    @Test
    void addCreatesNewBatchWith3MonthExpiry() {

        InventoryUpdateRequest r = new InventoryUpdateRequest("sku-add", 7, null);
        strategy.applyUpdate(r);

        ArgumentCaptor<InventoryBatch> cap = ArgumentCaptor.forClass(InventoryBatch.class);
        verify(repo).save(cap.capture());
        InventoryBatch saved = cap.getValue();
        assertEquals("sku-add", saved.getSku());
        assertEquals(7, saved.getQuantity());
        assertNotNull(saved.getExpiryDate());
        assertTrue(saved.getExpiryDate().isAfter(LocalDate.now().plusMonths(2)));
    }

    @Test
    void consumeAllocatesFromLargestFirst() {

        InventoryBatch b1 = InventoryBatch.builder().id(1L).sku("sku-b").quantity(2).expiryDate(LocalDate.now().plusDays(10)).build();
        InventoryBatch b2 = InventoryBatch.builder().id(2L).sku("sku-b").quantity(10).expiryDate(LocalDate.now().plusDays(20)).build();
        InventoryBatch b3 = InventoryBatch.builder().id(3L).sku("sku-b").quantity(5).expiryDate(LocalDate.now().plusDays(30)).build();

        when(repo.findBySkuOrderByExpiryDateAsc("sku-b")).thenReturn(List.of(b1, b2, b3));

        InventoryUpdateRequest r = new InventoryUpdateRequest("sku-b", -6, null);
        strategy.applyUpdate(r);

        ArgumentCaptor<InventoryBatch> cap = ArgumentCaptor.forClass(InventoryBatch.class);
        verify(repo, atLeastOnce()).save(cap.capture());
        List<InventoryBatch> saved = cap.getAllValues();
        // Expect the largest batch (10) to be reduced by 6 -> 4
        assertTrue(saved.stream().anyMatch(b -> b.getId()!=null && b.getId().equals(2L) && b.getQuantity()==4));
    }

    @Test
    void consumeInsufficient_throws() {
        InventoryBatch b1 = InventoryBatch.builder().id(1L).sku("sku-c").quantity(2).expiryDate(LocalDate.now().plusDays(10)).build();
        when(repo.findBySkuOrderByExpiryDateAsc("sku-c")).thenReturn(List.of(b1));

        InventoryUpdateRequest r = new InventoryUpdateRequest("sku-c", -5, null);
        assertThrows(IllegalStateException.class, () -> strategy.applyUpdate(r));
    }
}

