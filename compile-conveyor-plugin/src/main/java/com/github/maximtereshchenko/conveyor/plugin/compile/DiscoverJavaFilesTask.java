package com.github.maximtereshchenko.conveyor.plugin.compile;

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

final class DiscoverJavaFilesTask implements ConveyorTask {

    private final String name;
    private final Path path;
    private final ProductType productType;
    private final SchematicCoordinates coordinates;

    DiscoverJavaFilesTask(
        String name,
        Path path,
        ProductType productType,
        SchematicCoordinates coordinates
    ) {
        this.name = name;
        this.path = path;
        this.productType = productType;
        this.coordinates = coordinates;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        if (!Files.exists(path)) {
            return Set.of();
        }
        return files(path)
            .stream()
            .filter(file -> file.toString().endsWith(".java"))
            .map(file -> new Product(coordinates, file, productType))
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
