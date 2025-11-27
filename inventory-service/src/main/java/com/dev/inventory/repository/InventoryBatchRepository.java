package com.dev.inventory.repository;

import com.dev.inventory.domain.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {

    List<InventoryBatch> findBySkuOrderByExpiryDateAsc(String sku);
}

