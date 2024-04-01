package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class ProductRepository implements Repository {

    private final Set<Product> products;

    ProductRepository(Set<Product> products) {
        this.products = products;
    }

    @Override
    public Optional<Path> path(
        String group,
        String name,
        SemanticVersion semanticVersion,
        Classifier classifier
    ) {
        return products.stream()
            .filter(product ->
                hasCoordinates(product.schematicCoordinates(), group, name, semanticVersion)
            )
            .filter(product -> product.type() == productType(classifier))
            .map(Product::path)
            .findAny();
    }

    private ProductType productType(Classifier classifier) {
        return switch (classifier) {
            case SCHEMATIC_DEFINITION -> ProductType.SCHEMATIC_DEFINITION;
            case MODULE -> ProductType.MODULE;
        };
    }

    private boolean hasCoordinates(
        SchematicCoordinates schematicCoordinates,
        String group,
        String name,
        SemanticVersion semanticVersion
    ) {
        return schematicCoordinates.group().equals(group) &&
               schematicCoordinates.name().equals(name) &&
               new SemanticVersion(schematicCoordinates.version()).equals(semanticVersion);
    }
}
