package com.github.maximtereshchenko.conveyor.plugin.resources.test;

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.resources.ResourcesPlugin;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.test.common.Directories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.params.provider.Arguments.arguments;

final class CopyResourcesTests {

    private final ConveyorPlugin plugin = new ResourcesPlugin();

    public static Stream<Arguments> resources() {
        return Directories.differentDirectoryEntries()
            .flatMap(resources ->
                Stream.of(
                    arguments("main", resources, ProductType.EXPLODED_JAR, ProductType.RESOURCE),
                    arguments(
                        "test",
                        resources,
                        ProductType.EXPLODED_TEST_JAR,
                        ProductType.TEST_RESOURCE
                    )
                )
            );
    }

    @Test
    void givenPlugin_whenBindings_thenCopyResourcesBindingReturned(@TempDir Path path) {
        assertThat(
            plugin.bindings(
                FakeConveyorSchematicBuilder.discoveryDirectory(path).build(),
                Map.of()
            )
        )
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(tuple(Stage.COMPILE, Step.FINALIZE), tuple(Stage.TEST, Step.PREPARE));
    }

    @ParameterizedTest
    @EnumSource(value = ProductType.class, names = {"EXPLODED_JAR", "EXPLODED_TEST_JAR"})
    void givenNoResources_whenExecuteTasks_thenExplodedJarIsNotModified(
        ProductType productType,
        @TempDir Path path
    ) throws IOException {
        var explodedJar = Files.createDirectory(path.resolve("exploded-jar"));
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();
        var product = new Product(schematic.coordinates(), explodedJar, productType);

        var products = ConveyorTasks.executeTasks(schematic, plugin, product);

        assertThat(explodedJar).isEmptyDirectory();
        assertThat(products).containsExactly(product);
    }

    @ParameterizedTest
    @MethodSource("resources")
    void givenResources_whenExecuteTasks_thenExplodedJarContainsResources(
        String sourceSet,
        Set<String> resources,
        ProductType productType,
        ProductType resourceType,
        @TempDir Path path
    ) throws IOException {
        var explodedJar = path.resolve("exploded-jar");
        var resourcesDirectory = Directories.writeFiles(
            resourcesDirectory(path, sourceSet),
            resources
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        var products = ConveyorTasks.executeTasks(
            schematic,
            plugin,
            new Product(schematic.coordinates(), explodedJar, productType)
        );

        Directories.assertThatDirectoryContentsEqual(explodedJar, resourcesDirectory);
        assertThat(products)
            .filteredOn(product -> product.type() == resourceType)
            .map(Product::path)
            .containsOnly(Directories.files(resourcesDirectory).toArray(Path[]::new));
    }

    @ParameterizedTest
    @ValueSource(strings = {"main", "test"})
    void givenNoExplodedJar_whenExecuteTasks_thenNoFilesCopied(
        String sourceSet,
        @TempDir Path path
    ) throws IOException {
        Files.createDirectories(resourcesDirectory(path, sourceSet));
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .build();

        assertThatCode(() -> ConveyorTasks.executeTasks(schematic, plugin))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource(
        textBlock = """
                    main, EXPLODED_JAR
                    test, EXPLODED_TEST_JAR
                    """
    )
    void givenProductsFromOtherSchematics_whenExecuteTasks_thenFilesCopiedToCurrentSchematic(
        String sourceSet,
        ProductType productType,
        @TempDir Path path
    ) throws IOException {
        var explodedJar = path.resolve("exploded-jar");
        var otherExplodedJar = Files.createDirectories(path.resolve("other-exploded-jar"));
        var resourcesDirectory = Directories.writeFiles(
            resourcesDirectory(path, sourceSet),
            Set.of("file")
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        ConveyorTasks.executeTasks(
            schematic,
            plugin,
            new Product(
                new SchematicCoordinates(
                    "group",
                    "other-schematic",
                    "1.0.0"
                ),
                otherExplodedJar,
                productType
            ),
            new Product(schematic.coordinates(), explodedJar, productType)
        );

        assertThat(otherExplodedJar).isEmptyDirectory();
        Directories.assertThatDirectoryContentsEqual(explodedJar, resourcesDirectory);
    }

    private Path resourcesDirectory(Path path, String sourceSet) {
        return path.resolve("src").resolve(sourceSet).resolve("resources");
    }
}
