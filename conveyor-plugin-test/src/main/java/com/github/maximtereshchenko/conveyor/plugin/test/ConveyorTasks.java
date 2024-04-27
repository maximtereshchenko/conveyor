package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ConveyorTasks {

    public static Set<Product> executeTasks(
        ConveyorSchematic schematic,
        ConveyorPlugin plugin,
        Product... initial
    ) {
        var products = new HashSet<>(Set.of(initial));
        for (var binding : plugin.bindings(schematic, Map.of())) {
            products.addAll(binding.task().execute(products));
        }
        return products;
    }
}
