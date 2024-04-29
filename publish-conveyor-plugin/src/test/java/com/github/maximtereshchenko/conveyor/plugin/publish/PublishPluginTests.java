package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.conveyor.plugin.test.PublishedArtifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

final class PublishPluginTests {

    private final ConveyorPlugin plugin = new PublishPlugin();

    @Test
    void givenPlugin_whenBindings_thenCopyResourcesBindingReturned(@TempDir Path path) {
        assertThat(
            plugin.bindings(
                FakeConveyorSchematicBuilder.discoveryDirectory(path).build(),
                Map.of()
            )
        )
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .containsExactly(tuple(Stage.PUBLISH, Step.RUN));
    }

    @ParameterizedTest
    @CsvSource(
        textBlock = """
                    JAR, JAR
                    SCHEMATIC_DEFINITION, SCHEMATIC_DEFINITION
                    """
    )
    void givenProduct_whenExecuteTasks_thenProductIsPublished(
        ProductType productType,
        ArtifactClassifier artifactClassifier,
        @TempDir Path path
    ) {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();
        var product = path.resolve("product");

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of("repository", "repository")),
            new Product(schematic.coordinates(), product, productType)
        );

        assertThat(schematic.published())
            .containsExactly(new PublishedArtifact("repository", product, artifactClassifier));
    }

    @Test
    void givenProductsFromOtherSchematics_whenExecuteTasks_thenProductsPublishedToCurrentSchematic(
        @TempDir Path path
    ) {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();
        var product = path.resolve("product");

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of("repository", "repository")),
            new Product(
                new SchematicCoordinates("group", "other-schematic", "1.0.0"),
                path.resolve("other-product"),
                ProductType.JAR
            ),
            new Product(schematic.coordinates(), product, ProductType.JAR)
        );

        assertThat(schematic.published())
            .containsExactly(new PublishedArtifact("repository", product, ArtifactClassifier.JAR));
    }
}
