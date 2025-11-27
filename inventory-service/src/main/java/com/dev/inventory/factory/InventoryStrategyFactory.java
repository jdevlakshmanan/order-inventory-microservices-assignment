package com.dev.inventory.factory;

import com.dev.inventory.strategy.InventoryStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class InventoryStrategyFactory {

    private final Map<String, InventoryStrategy> strategies;

    public InventoryStrategyFactory(List<InventoryStrategy> strategyList) {
        this.strategies = strategyList.stream().collect(Collectors.toMap(InventoryStrategy::getName, Function.identity()));
    }

    public InventoryStrategy get(String name) {
        return strategies.getOrDefault(name, strategies.values().stream().findFirst().orElseThrow());
    }
}

