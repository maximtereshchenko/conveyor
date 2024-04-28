package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

final class CopyResourcesTask implements ConveyorTask {

    private final ProductType destinationType;
    private final ProductType resourceType;
    private final SchematicCoordinates schematicCoordinates;
    private final Path base;

    CopyResourcesTask(
        ProductType destinationType,
        ProductType resourceType,
        SchematicCoordinates schematicCoordinates,
        Path base
    ) {
        this.destinationType = destinationType;
        this.resourceType = resourceType;
        this.schematicCoordinates = schematicCoordinates;
        this.base = base;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        paths(products, destinationType)
            .forEach(explodedJar ->
                paths(products, resourceType)
                    .forEach(resource -> copy(resource, explodedJar))
            );
        return Set.of();
    }

    private void copy(Path resource, Path explodedJar) {
        try {
            var destination = explodedJar.resolve(base.relativize(resource));
            Files.createDirectories(destination.getParent());
            Files.copy(resource, destination);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Stream<Path> paths(Set<Product> products, ProductType type) {
        return products.stream()
            .filter(product -> product.schematicCoordinates().equals(schematicCoordinates))
            .filter(product -> product.type() == type)
            .map(Product::path);
    }
}
