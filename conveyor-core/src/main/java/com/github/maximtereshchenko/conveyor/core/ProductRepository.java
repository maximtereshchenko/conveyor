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
        SemanticVersion semanticVersion,
        Classifier classifier
    ) {
        return productType(classifier)
            .flatMap(productType ->
                products.stream()
                    .filter(product ->
                        hasCoordinates(product.schematicCoordinates(), id, semanticVersion)
                    )
                    .filter(product -> product.type() == productType)
                    .map(Product::path)
                    .findAny()
            );
    }

    @Override
    public void publish(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier,
        Resource resource
    ) {
        throw new IllegalArgumentException();
    }

    private Optional<ProductType> productType(Classifier classifier) {
        return switch (classifier) {
            case SCHEMATIC_DEFINITION -> Optional.of(ProductType.SCHEMATIC_DEFINITION);
            case JAR -> Optional.of(ProductType.JAR);
            case POM -> Optional.empty();
        };
    }

    private boolean hasCoordinates(
        SchematicCoordinates schematicCoordinates,
        Id id,
        SemanticVersion semanticVersion
    ) {
        return schematicCoordinates.group().equals(id.group()) &&
               schematicCoordinates.name().equals(id.name()) &&
               new SemanticVersion(schematicCoordinates.version()).equals(semanticVersion);
    }
}
