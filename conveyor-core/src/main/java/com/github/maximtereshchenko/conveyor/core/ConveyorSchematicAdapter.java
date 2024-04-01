package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class ConveyorSchematicAdapter implements ConveyorSchematic {

    private final Dependencies dependencies;
    private final Properties properties;
    private final SchematicCoordinates schematicCoordinates;

    ConveyorSchematicAdapter(
        Dependencies dependencies,
        Properties properties,
        SchematicCoordinates schematicCoordinates
    ) {
        this.dependencies = dependencies;
        this.properties = properties;
        this.schematicCoordinates = schematicCoordinates;
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
    public Set<Path> modulePath(DependencyScope... scopes) {
        return dependencies.modulePath(Set.of(scopes));
    }

    @Override
    public Product product(Path path, ProductType type) {
        return new Product(schematicCoordinates, path, type);
    }
}
