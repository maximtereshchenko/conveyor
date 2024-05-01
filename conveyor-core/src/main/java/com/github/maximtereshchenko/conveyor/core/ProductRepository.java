package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class ProductRepository implements Repository<Path> {

    private final Set<Product> products;

    ProductRepository(Set<Product> products) {
        this.products = products;
    }

    @Override
    public boolean hasName(String name) {
        return false;
    }

    @Override
    public Optional<Path> artifact(
        Id id,
        Version version,
        Classifier classifier
    ) {
        return switch (classifier) {
            case SCHEMATIC_DEFINITION -> path(id, version, ProductType.SCHEMATIC_DEFINITION);
            case JAR -> path(id, version, ProductType.JAR)
                .or(() -> path(id, version, ProductType.EXPLODED_JAR));
            case POM -> Optional.empty();
        };
    }

    @Override
    public void publish(
        Id id,
        Version version,
        Classifier classifier,
        Resource resource
    ) {
        throw new IllegalArgumentException();
    }

    private Optional<Path> path(Id id, Version version, ProductType productType) {
        return products.stream()
            .filter(product ->
                hasCoordinates(product.schematicCoordinates(), id, version)
            )
            .filter(product -> product.type() == productType)
            .map(Product::path)
            .findAny();
    }

    private boolean hasCoordinates(
        SchematicCoordinates schematicCoordinates,
        Id id,
        Version version
    ) {
        return schematicCoordinates.group().equals(id.group()) &&
               schematicCoordinates.name().equals(id.name()) &&
               new Version(schematicCoordinates.version()).equals(version);
    }
}
