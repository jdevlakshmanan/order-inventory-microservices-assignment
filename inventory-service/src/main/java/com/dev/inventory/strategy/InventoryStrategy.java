package com.dev.inventory.strategy;

import com.dev.inventory.dto.InventoryUpdateRequest;

public interface InventoryStrategy {

    String getName();
    void applyUpdate(InventoryUpdateRequest request);
}

