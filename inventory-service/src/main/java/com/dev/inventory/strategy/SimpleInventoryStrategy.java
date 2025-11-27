package com.dev.inventory.strategy;

import com.dev.inventory.dto.InventoryUpdateRequest;
import com.dev.inventory.domain.InventoryBatch;
import com.dev.inventory.repository.InventoryBatchRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class SimpleInventoryStrategy implements InventoryStrategy {

    private final InventoryBatchRepository repository;

    public SimpleInventoryStrategy(InventoryBatchRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getName() {
        return "simple";
    }

    @Override
    @Transactional
    public void applyUpdate(InventoryUpdateRequest request) {

        if (request.getDelta() > 0) {
            InventoryBatch batch = InventoryBatch.builder()
                    .sku(request.getSku())
                    .quantity(request.getDelta())
                    .expiryDate(LocalDate.now().plusYears(1))
                    .build();
            repository.save(batch);
            return;
        }

        int toConsume = -request.getDelta();
        List<InventoryBatch> batches = repository.findBySkuOrderByExpiryDateAsc(request.getSku());

        for (InventoryBatch b : batches) {
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

