package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ConveyorTasks {

    public static Set<Product> executeTasks(
        List<ConveyorTaskBinding> bindings,
        Product... initial
    ) {
        var products = new HashSet<>(Set.of(initial));
        for (var binding : bindings) {
            products.addAll(binding.task().execute(products));
        }
        return products;
    }
}
