package com.dev.inventory.service.impl;

import com.dev.inventory.dto.InventoryBatchDto;
import com.dev.inventory.dto.InventoryUpdateRequest;
import com.dev.inventory.domain.InventoryBatch;
import com.dev.inventory.repository.InventoryBatchRepository;
import com.dev.inventory.service.InventoryService;
import com.dev.inventory.factory.InventoryStrategyFactory;
import com.dev.inventory.strategy.InventoryStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryBatchRepository repository;
    private final InventoryStrategyFactory strategyFactory;

    public InventoryServiceImpl(InventoryBatchRepository repository, InventoryStrategyFactory strategyFactory) {
        this.repository = repository;
        this.strategyFactory = strategyFactory;
    }

    @Override
    public List<InventoryBatchDto> getBatchesBySkuSortedByExpiry(String sku) {

        List<InventoryBatch> batches = repository.findBySkuOrderByExpiryDateAsc(sku);
        return batches.stream().map(b -> InventoryBatchDto.builder()
                .id(b.getId())
                .sku(b.getSku())
                .quantity(b.getQuantity())
                .expiryDate(b.getExpiryDate())
                .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(InventoryUpdateRequest request) {

        // delegate to selected strategy
        String strategyName = request.getStrategy();
        InventoryStrategy strategy = strategyFactory.get(strategyName);
        strategy.applyUpdate(request);
    }
}
