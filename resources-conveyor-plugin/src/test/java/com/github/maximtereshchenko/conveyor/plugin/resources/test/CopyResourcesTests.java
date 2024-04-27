package com.github.maximtereshchenko.conveyor.plugin.resources.test;

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.resources.ResourcesPlugin;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.test.common.Directories;
import com.github.maximtereshchenko.test.common.JimfsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@ExtendWith(JimfsExtension.class)
final class CopyResourcesTests {

    private final ConveyorPlugin plugin = new ResourcesPlugin();

    public static Stream<Arguments> resources() {
        return Directories.differentDirectoryEntries()
            .flatMap(resources ->
                Stream.of(
                    arguments("main", resources, ProductType.EXPLODED_MODULE, ProductType.RESOURCE),
                    arguments(
                        "test",
                        resources,
                        ProductType.EXPLODED_TEST_MODULE,
                        ProductType.TEST_RESOURCE
                    )
                )
            );
    }

    @Test
    void givenPlugin_whenBindings_thenCopyResourcesBindingReturned(Path path) {
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
    @EnumSource(value = ProductType.class, names = {"EXPLODED_MODULE", "EXPLODED_TEST_MODULE"})
    void givenNoResources_whenExecuteTasks_thenExplodedModuleIsNotModified(
        ProductType productType,
        Path path
    ) throws IOException {
        var explodedModule = Files.createDirectory(path.resolve("exploded-module"));
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();
        var product = new Product(schematic.coordinates(), explodedModule, productType);

        var products = ConveyorTasks.executeTasks(schematic, plugin, product);

        assertThat(explodedModule).isEmptyDirectory();
        assertThat(products).containsExactly(product);
    }

    @ParameterizedTest
    @MethodSource("resources")
    void givenResources_whenExecuteTasks_thenExplodedModuleContainsResources(
        String sourceSet,
        Set<String> resources,
        ProductType productType,
        ProductType resourceType,
        Path path
    ) throws IOException {
        var explodedModule = path.resolve("exploded-module");
        var resourcesDirectory = Directories.writeFiles(
            resourcesDirectory(path, sourceSet),
            resources
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        var products = ConveyorTasks.executeTasks(
            schematic,
            plugin,
            new Product(schematic.coordinates(), explodedModule, productType)
        );

        Directories.assertThatDirectoryContentsEqual(explodedModule, resourcesDirectory);
        assertThat(products)
            .filteredOn(product -> product.type() == resourceType)
            .map(Product::path)
            .containsOnly(Directories.files(resourcesDirectory).toArray(Path[]::new));
    }

    @ParameterizedTest
    @ValueSource(strings = {"main", "test"})
    void givenNoExplodedModule_whenExecuteTasks_thenNoFilesCopied(String sourceSet, Path path)
        throws IOException {
        Files.createDirectories(resourcesDirectory(path, sourceSet));
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .build();

        assertThatCode(() -> ConveyorTasks.executeTasks(schematic, plugin))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource(
        textBlock = """
                    main, EXPLODED_MODULE
                    test, EXPLODED_TEST_MODULE
                    """
    )
    void givenProductsFromOtherSchematics_whenExecuteTasks_thenFilesCopiedToCurrentSchematic(
        String sourceSet,
        ProductType productType,
        Path path
    ) throws IOException {
        var explodedModule = path.resolve("exploded-module");
        var otherExplodedModule = Files.createDirectories(path.resolve("other-exploded-module"));
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
                otherExplodedModule,
                productType
            ),
            new Product(schematic.coordinates(), explodedModule, productType)
        );

        assertThat(otherExplodedModule).isEmptyDirectory();
        Directories.assertThatDirectoryContentsEqual(explodedModule, resourcesDirectory);
    }

    private Path resourcesDirectory(Path path, String sourceSet) {
        return path.resolve("src").resolve(sourceSet).resolve("resources");
    }
}
