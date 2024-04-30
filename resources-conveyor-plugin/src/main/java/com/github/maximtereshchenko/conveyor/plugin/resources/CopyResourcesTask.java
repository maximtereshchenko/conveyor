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
import java.util.stream.Collectors;

final class CopyResourcesTask implements ConveyorTask {

    private static final System.Logger LOGGER = System.getLogger(CopyResourcesTask.class.getName());

    private final String name;
    private final ProductType destinationType;
    private final ProductType resourceType;
    private final SchematicCoordinates schematicCoordinates;
    private final Path base;

    CopyResourcesTask(
        String name,
        ProductType destinationType,
        ProductType resourceType,
        SchematicCoordinates schematicCoordinates,
        Path base
    ) {
        this.name = name;
        this.destinationType = destinationType;
        this.resourceType = resourceType;
        this.schematicCoordinates = schematicCoordinates;
        this.base = base;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        var destinations = paths(products, destinationType);
        if (destinations.isEmpty()) {
            LOGGER.log(System.Logger.Level.WARNING, "No destinations to copy to");
            return Set.of();
        }
        var resources = paths(products, resourceType);
        if (destinations.isEmpty()) {
            LOGGER.log(System.Logger.Level.WARNING, "No resources to copy");
            return Set.of();
        }
        destinations.forEach(destination ->
            resources.forEach(resource -> copy(resource, destination))
        );
        return Set.of();
    }

    private void copy(Path resource, Path explodedJar) {
        try {
            var destination = explodedJar.resolve(base.relativize(resource));
            Files.createDirectories(destination.getParent());
            Files.copy(resource, destination);
            LOGGER.log(System.Logger.Level.INFO, "Copied {0} to {1}", resource, destination);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Set<Path> paths(Set<Product> products, ProductType type) {
        return products.stream()
            .filter(product -> product.schematicCoordinates().equals(schematicCoordinates))
            .filter(product -> product.type() == type)
            .map(Product::path)
            .collect(Collectors.toSet());
    }
}
