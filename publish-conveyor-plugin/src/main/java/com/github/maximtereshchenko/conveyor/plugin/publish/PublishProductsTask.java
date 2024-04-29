package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.util.Optional;
import java.util.Set;

final class PublishProductsTask implements ConveyorTask {

    private final ConveyorSchematic conveyorSchematic;
    private final String repository;

    PublishProductsTask(ConveyorSchematic conveyorSchematic, String repository) {
        this.conveyorSchematic = conveyorSchematic;
        this.repository = repository;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        products.stream()
            .filter(product ->
                product.schematicCoordinates().equals(conveyorSchematic.coordinates())
            )
            .forEach(product ->
                artifactClassifier(product.type())
                    .ifPresent(artifactClassifier ->
                        conveyorSchematic.publish(repository, product.path(), artifactClassifier))
            );
        return Set.of();
    }

    private Optional<ArtifactClassifier> artifactClassifier(ProductType productType) {
        return switch (productType) {
            case SOURCE, RESOURCE, EXPLODED_JAR, TEST_SOURCE, TEST_RESOURCE, EXPLODED_TEST_JAR ->
                Optional.empty();
            case SCHEMATIC_DEFINITION -> Optional.of(ArtifactClassifier.SCHEMATIC_DEFINITION);
            case JAR -> Optional.of(ArtifactClassifier.JAR);
        };
    }
}
