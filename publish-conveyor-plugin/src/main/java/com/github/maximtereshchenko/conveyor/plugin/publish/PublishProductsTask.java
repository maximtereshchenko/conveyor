package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class PublishProductsTask implements ConveyorTask {

    private static final System.Logger LOGGER =
        System.getLogger(PublishProductsTask.class.getName());

    private final ConveyorSchematic conveyorSchematic;
    private final String repository;

    PublishProductsTask(ConveyorSchematic conveyorSchematic, String repository) {
        this.conveyorSchematic = conveyorSchematic;
        this.repository = repository;
    }

    @Override
    public String name() {
        return "publish-products";
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        var publications = publications(products);
        if (publications.isEmpty()) {
            LOGGER.log(System.Logger.Level.WARNING, "Nothing to publish");
        }
        publications.forEach(Runnable::run);
        return Set.of();
    }

    private List<Runnable> publications(Set<Product> products) {
        return products.stream()
            .filter(product ->
                product.schematicCoordinates().equals(conveyorSchematic.coordinates())
            )
            .flatMap(product ->
                artifactClassifier(product.type())
                    .map(artifactClassifier -> publication(product.path(), artifactClassifier))
                    .stream()
            )
            .toList();
    }

    private Runnable publication(Path path, ArtifactClassifier artifactClassifier) {
        return () -> {
            conveyorSchematic.publish(repository, path, artifactClassifier);
            LOGGER.log(
                System.Logger.Level.INFO,
                "Published {0}:{1} to {2}",
                path,
                artifactClassifier,
                repository
            );
        };
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
