package com.dev.inventory.controller;

import com.dev.inventory.dto.InventoryBatchDto;
import com.dev.inventory.dto.InventoryUpdateRequest;
import com.dev.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<InventoryBatchDto>> getBatches(@PathVariable("productId") String sku) {
        return ResponseEntity.ok(inventoryService.getBatchesBySkuSortedByExpiry(sku));
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateInventory(@RequestBody @Valid InventoryUpdateRequest request) {
        inventoryService.updateInventory(request);
        return ResponseEntity.ok("Inventory updated successfully");
    }
}
