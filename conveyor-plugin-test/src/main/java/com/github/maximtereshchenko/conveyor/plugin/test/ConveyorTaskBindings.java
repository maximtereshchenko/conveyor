package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;

import java.util.*;

public final class ConveyorTaskBindings {

    private final List<ConveyorTaskBinding> bindings;

    private ConveyorTaskBindings(List<ConveyorTaskBinding> bindings) {
        this.bindings = bindings;
    }

    public static ConveyorTaskBindings from(ConveyorSchematic conveyorSchematic) {
        return new ConveyorTaskBindings(
            ServiceLoader.load(ConveyorPlugin.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .map(conveyorPlugin -> conveyorPlugin.bindings(conveyorSchematic, Map.of()))
                .flatMap(Collection::stream)
                .toList()
        );
    }

    public ListAssert<ConveyorTaskBinding> assertThat() {
        return Assertions.assertThat(bindings);
    }

    public Set<Product> executeTasks(Product... initial) {
        var products = new HashSet<>(Set.of(initial));
        for (var binding : bindings) {
            products.addAll(binding.task().execute(products));
        }
        return products;
    }
}
