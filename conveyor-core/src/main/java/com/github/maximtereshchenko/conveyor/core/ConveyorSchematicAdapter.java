package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class ConveyorSchematicAdapter implements ConveyorSchematic {

    private final SchematicCoordinates schematicCoordinates;
    private final Properties properties;
    private final Dependencies dependencies;

    ConveyorSchematicAdapter(
        SchematicCoordinates schematicCoordinates,
        Properties properties,
        Dependencies dependencies
    ) {
        this.schematicCoordinates = schematicCoordinates;
        this.properties = properties;
        this.dependencies = dependencies;
    }

    @Override
    public SchematicCoordinates coordinates() {
        return schematicCoordinates;
    }

    @Override
    public Path discoveryDirectory() {
        return properties.discoveryDirectory();
    }

    @Override
    public Path constructionDirectory() {
        return properties.constructionDirectory();
    }

    @Override
    public Optional<String> propertyValue(String key) {
        return properties.value(key);
    }

    @Override
    public Set<Path> modulePath(Set<DependencyScope> scopes) {
        return dependencies.modulePath(scopes);
    }
}
