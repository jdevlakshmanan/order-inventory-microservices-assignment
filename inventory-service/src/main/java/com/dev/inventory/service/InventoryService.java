package com.dev.inventory.service;

import com.dev.inventory.dto.InventoryBatchDto;
import com.dev.inventory.dto.InventoryUpdateRequest;

import java.util.List;

public interface InventoryService {

    List<InventoryBatchDto> getBatchesBySkuSortedByExpiry(String sku);
    void updateInventory(InventoryUpdateRequest request);
}

