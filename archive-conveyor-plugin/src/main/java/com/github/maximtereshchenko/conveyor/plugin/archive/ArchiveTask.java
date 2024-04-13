package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.zip.ArchiveContainer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

final class ArchiveTask implements ConveyorTask {

    private final Path target;
    private final ConveyorSchematic schematic;

    ArchiveTask(Path target, ConveyorSchematic schematic) {
        this.target = target;
        this.schematic = schematic;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        return products.stream()
            .filter(product -> product.type() == ProductType.EXPLODED_MODULE)
            .map(Product::path)
            .map(this::module)
            .collect(Collectors.toSet());
    }

    private Product module(Path explodedModule) {
        try {
            Files.createDirectories(target.getParent());
            new ArchiveContainer(explodedModule).archive(target);
            return schematic.product(target, ProductType.MODULE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
