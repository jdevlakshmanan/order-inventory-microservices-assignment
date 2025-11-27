package com.dev.inventory.strategy;

import com.dev.inventory.dto.InventoryUpdateRequest;
import com.dev.inventory.domain.InventoryBatch;
import com.dev.inventory.repository.InventoryBatchRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
public class BatchAllocationStrategy implements InventoryStrategy {

    private final InventoryBatchRepository repository;

    public BatchAllocationStrategy(InventoryBatchRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getName() {
        return "batch";
    }

    @Override
    @Transactional
    public void applyUpdate(InventoryUpdateRequest request) {

        // When adding stock (delta>0) create a new batch with a 3-month expiry.
        // When consuming (delta<0) allocate from the largest batches first (quantity-descending) to reduce fragmentation.
        if (request.getDelta() > 0) {
            InventoryBatch batch = InventoryBatch.builder()
                    .sku(request.getSku())
                    .quantity(request.getDelta())
                    .expiryDate(LocalDate.now().plusMonths(3))
                    .build();
            repository.save(batch);
            return;
        }

        int toConsume = -request.getDelta();
        List<InventoryBatch> batches = repository.findBySkuOrderByExpiryDateAsc(request.getSku());
        // Sort by quantity descending to consume from largest batches first
        List<InventoryBatch> bySizeDesc = batches.stream()
                .sorted(Comparator.comparingInt(InventoryBatch::getQuantity).reversed())
                .toList();

        for (InventoryBatch b : bySizeDesc) {
            if (toConsume <= 0) break;
            int avail = b.getQuantity();
            if (avail <= 0) continue;
            int take = Math.min(avail, toConsume);
            b.setQuantity(avail - take);
            toConsume -= take;
            repository.save(b);
        }

        if (toConsume > 0) {
            throw new IllegalStateException("Not enough inventory for sku=" + request.getSku());
        }
    }
}
