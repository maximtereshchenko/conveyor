package com.github.maximtereshchenko.conveyor.plugin.resources.test;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class FakeConveyorSchematic implements ConveyorSchematic {

    private final Path discoveryDirectory;

    FakeConveyorSchematic(Path path) {
        discoveryDirectory = path;
    }

    @Override
    public Path discoveryDirectory() {
        return discoveryDirectory;
    }

    @Override
    public Path constructionDirectory() {
        throw new IllegalArgumentException();
    }

    @Override
    public Optional<String> propertyValue(String key) {
        throw new IllegalArgumentException();
    }

    @Override
    public Set<Path> modulePath(Set<DependencyScope> scopes) {
        throw new IllegalArgumentException();
    }

    @Override
    public Product product(Path path, ProductType type) {
        return new Product(new SchematicCoordinates("", "", ""), path, type);
    }
}
