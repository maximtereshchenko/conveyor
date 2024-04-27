package com.github.maximtereshchenko.conveyor.plugin.archive.test;

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.archive.ArchivePlugin;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.test.common.Directories;
import com.github.maximtereshchenko.test.common.JimfsExtension;
import com.github.maximtereshchenko.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JimfsExtension.class)
final class ArchivePluginTests {

    private final ConveyorPlugin plugin = new ArchivePlugin();

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @Test
    void givenPlugin_whenBindings_thenTaskBindToArchiveRun(Path path) {
        assertThat(
            plugin.bindings(
                FakeConveyorSchematicBuilder.discoveryDirectory(path).build(),
                Map.of()
            )
        )
            .hasSize(1)
            .first()
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .containsExactly(Stage.ARCHIVE, Step.RUN);
    }

    @Test
    void givenNoExplodedModule_whenExecuteTask_thenNoModuleProduct(Path path) {
        var products = ConveyorTasks.executeTasks(
            FakeConveyorSchematicBuilder.discoveryDirectory(path)
                .build(),
            plugin
        );

        assertThat(products).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("entries")
    void givenExplodedModule_whenExecuteTask_thenModuleContainsFiles(
        Set<String> entries,
        Path path
    ) throws IOException {
        var explodedModule = path.resolve("exploded-module");
        Directories.writeFiles(explodedModule, entries);
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        var products = ConveyorTasks.executeTasks(
            schematic,
            plugin,
            new Product(schematic.coordinates(), explodedModule, ProductType.EXPLODED_MODULE)
        );

        assertThat(products)
            .filteredOn(product -> product.type() == ProductType.MODULE)
            .map(Product::path)
            .first()
            .satisfies(module -> {
                var extracted = Files.createDirectory(path.resolve("extracted"));
                new ZipArchive(module).extract(extracted);
                Directories.assertThatDirectoryContentsEqual(extracted, explodedModule);
            });
    }

    @Test
    void givenProductsFromDifferentSchematic_whenExecuteTask_thenArchiveCreatedForCurrentSchematic(
        Path path
    ) throws IOException {
        var explodedModule = path.resolve("exploded-module");
        Files.createFile(Directories.createDirectoriesForFile(explodedModule.resolve("file")));
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        var products = ConveyorTasks.executeTasks(
            schematic,
            plugin,
            new Product(
                new SchematicCoordinates(
                    "group",
                    "other-schematic",
                    "1.0.0"
                ),
                path.resolve("incorrect"),
                ProductType.EXPLODED_MODULE
            ),
            new Product(schematic.coordinates(), explodedModule, ProductType.EXPLODED_MODULE)
        );

        assertThat(products)
            .filteredOn(product -> product.type() == ProductType.MODULE)
            .map(Product::path)
            .first()
            .satisfies(module -> {
                var extracted = Files.createDirectory(path.resolve("extracted"));
                new ZipArchive(module).extract(extracted);
                Directories.assertThatDirectoryContentsEqual(extracted, explodedModule);
            });
    }
}
