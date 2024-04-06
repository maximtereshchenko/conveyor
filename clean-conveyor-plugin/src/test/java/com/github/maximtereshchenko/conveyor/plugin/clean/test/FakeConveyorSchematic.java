package com.github.maximtereshchenko.conveyor.plugin.clean.test;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class FakeConveyorSchematic implements ConveyorSchematic {

    private final Path constructionDirectory;

    FakeConveyorSchematic(Path constructionDirectory) {
        this.constructionDirectory = constructionDirectory;
    }

    @Override
    public Path discoveryDirectory() {
        throw new IllegalArgumentException();
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
    public Set<Path> modulePath(DependencyScope... scopes) {
        throw new IllegalArgumentException();
    }

    @Override
    public Product product(Path path, ProductType type) {
        throw new IllegalArgumentException();
    }
}
