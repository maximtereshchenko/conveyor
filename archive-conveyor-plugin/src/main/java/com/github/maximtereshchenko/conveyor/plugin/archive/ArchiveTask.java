package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
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
    private final SchematicCoordinates coordinates;

    ArchiveTask(Path target, SchematicCoordinates coordinates) {
        this.target = target;
        this.coordinates = coordinates;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        return products.stream()
            .filter(product -> product.schematicCoordinates().equals(coordinates))
            .filter(product -> product.type() == ProductType.EXPLODED_JAR)
            .map(Product::path)
            .map(this::jar)
            .collect(Collectors.toSet());
    }

    private Product jar(Path explodedModule) {
        try {
            Files.createDirectories(target.getParent());
            new ArchiveContainer(explodedModule).archive(target);
            return new Product(coordinates, target, ProductType.JAR);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
