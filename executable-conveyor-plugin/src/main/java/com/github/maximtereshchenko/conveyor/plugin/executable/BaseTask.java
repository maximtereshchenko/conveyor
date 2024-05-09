package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

abstract class BaseTask implements ConveyorTask {

    private final ConveyorSchematic schematic;

    BaseTask(ConveyorSchematic schematic) {
        this.schematic = schematic;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        explodedJar(products).ifPresent(explodedJar -> onExplodedJar(schematic, explodedJar));
        return Set.of();
    }

    abstract void onExplodedJar(ConveyorSchematic schematic, Path explodedJar);

    private Optional<Path> explodedJar(Set<Product> products) {
        return products.stream()
            .filter(product -> product.schematicCoordinates().equals(schematic.coordinates()))
            .filter(product -> product.type() == ProductType.EXPLODED_JAR)
            .map(Product::path)
            .findAny();
    }
}
