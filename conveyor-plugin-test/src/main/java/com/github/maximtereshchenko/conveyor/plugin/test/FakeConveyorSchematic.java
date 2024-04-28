package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class FakeConveyorSchematic implements ConveyorSchematic {

    private final Path discoveryDirectory;
    private final Path constructionDirectory;
    private final Set<Path> dependencies;

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
    public SchematicCoordinates coordinates() {
        return new SchematicCoordinates("", "", "");
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Path> classPath(Set<DependencyScope> scopes) {
        return dependencies;
    }
}
