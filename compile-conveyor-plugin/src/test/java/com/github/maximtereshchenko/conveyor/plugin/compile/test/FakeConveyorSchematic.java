package com.github.maximtereshchenko.conveyor.plugin.compile.test;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class FakeConveyorSchematic implements ConveyorSchematic {

    private final Path constructionDirectory;
    private final Set<Path> dependencies;
    private final Path discoveryDirectory;

    FakeConveyorSchematic(
        Path discoveryDirectory,
        Path constructionDirectory,
        Set<Path> dependencies
    ) {
        this.discoveryDirectory = discoveryDirectory;
        this.constructionDirectory = constructionDirectory;
        this.dependencies = dependencies;
    }

    @Override
    public Path discoveryDirectory() {
        return discoveryDirectory;
    }

    @Override
    public Path constructionDirectory() {
        return constructionDirectory;
    }

    @Override
    public Optional<String> propertyValue(String key) {
        throw new IllegalArgumentException();
    }

    @Override
    public Set<Path> modulePath(Set<DependencyScope> scopes) {
        return dependencies;
    }

    @Override
    public Product product(Path path, ProductType type) {
        return new Product(new SchematicCoordinates("", "", ""), path, type);
    }
}
