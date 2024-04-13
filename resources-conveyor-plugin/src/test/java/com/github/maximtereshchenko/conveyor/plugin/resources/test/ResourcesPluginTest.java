package com.github.maximtereshchenko.conveyor.plugin.resources.test;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.jimfs.JimfsExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@ExtendWith(JimfsExtension.class)
abstract class ResourcesPluginTest {

    void writeResource(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, path.getFileName().toString());
    }

    Set<Product> executeTasks(Path discoveryDirectory, Path target, ProductType targetType)
        throws IOException {
        var schematic = new FakeConveyorSchematic(discoveryDirectory);
        var products = new HashSet<Product>();
        products.add(schematic.product(Files.createDirectories(target), targetType));
        for (var binding : bindings(discoveryDirectory)) {
            products.addAll(binding.task().execute(products));
        }
        return products;
    }

    List<ConveyorTaskBinding> bindings(Path discoveryDirectory) {
        return bindings(new FakeConveyorSchematic(discoveryDirectory));
    }

    List<ConveyorTaskBinding> bindings(ConveyorSchematic schematic) {
        return ServiceLoader.load(ConveyorPlugin.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .map(conveyorPlugin -> conveyorPlugin.bindings(schematic, Map.of()))
            .flatMap(Collection::stream)
            .toList();
    }
}
