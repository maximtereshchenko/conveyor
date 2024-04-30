package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

final class DiscoverResourcesTask implements ConveyorTask {

    private final String name;
    private final Path directory;
    private final ProductType resourceType;
    private final SchematicCoordinates schematicCoordinates;

    DiscoverResourcesTask(
        String name,
        Path directory,
        ProductType resourceType,
        SchematicCoordinates schematicCoordinates
    ) {
        this.name = name;
        this.directory = directory;
        this.resourceType = resourceType;
        this.schematicCoordinates = schematicCoordinates;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        if (!Files.exists(directory)) {
            return Set.of();
        }
        return files(directory)
            .stream()
            .map(resource -> new Product(schematicCoordinates, resource, resourceType))
            .collect(Collectors.toSet());
    }

    private Set<Path> files(Path path) {
        if (Files.isRegularFile(path)) {
            return Set.of(path);
        }
        try (var stream = Files.list(path)) {
            return stream.map(this::files)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
