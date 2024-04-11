package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

final class DiscoverJavaFilesTask implements ConveyorTask {

    private final Path path;
    private final ProductType productType;
    private final ConveyorSchematic schematic;

    DiscoverJavaFilesTask(Path path, ProductType productType, ConveyorSchematic schematic) {
        this.path = path;
        this.productType = productType;
        this.schematic = schematic;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        if (!Files.exists(path)) {
            return Set.of();
        }
        return files(path)
            .stream()
            .filter(file -> file.toString().endsWith(".java"))
            .map(file -> schematic.product(file, productType))
            .collect(Collectors.toSet());
    }

    Set<Path> files(Path path) {
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
