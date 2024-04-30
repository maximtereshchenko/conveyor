package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.zip.ZipArchiveContainer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

final class ArchiveTask implements ConveyorTask {

    private static final System.Logger LOGGER = System.getLogger(ArchiveTask.class.getName());

    private final Path target;
    private final SchematicCoordinates coordinates;

    ArchiveTask(Path target, SchematicCoordinates coordinates) {
        this.target = target;
        this.coordinates = coordinates;
    }

    @Override
    public String name() {
        return "archive";
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        var jars = products.stream()
            .filter(product -> product.schematicCoordinates().equals(coordinates))
            .filter(product -> product.type() == ProductType.EXPLODED_JAR)
            .map(Product::path)
            .map(this::jar)
            .collect(Collectors.toSet());
        if (jars.isEmpty()) {
            LOGGER.log(System.Logger.Level.WARNING, "Nothing to archive");
        }
        return jars;
    }

    private Product jar(Path explodedJar) {
        try {
            Files.createDirectories(target.getParent());
            new ZipArchiveContainer(explodedJar).archive(target);
            LOGGER.log(
                System.Logger.Level.INFO,
                "Archived {0} to {1}",
                explodedJar,
                target
            );
            return new Product(coordinates, target, ProductType.JAR);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
