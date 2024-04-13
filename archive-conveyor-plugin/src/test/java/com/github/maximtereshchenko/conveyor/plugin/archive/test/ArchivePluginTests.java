package com.github.maximtereshchenko.conveyor.plugin.archive.test;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTaskBindings;
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
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JimfsExtension.class)
final class ArchivePluginTests {

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @Test
    void givenPlugin_whenBindings_thenTaskBindToArchiveRun(Path path) {
        ConveyorTaskBindings.from(FakeConveyorSchematicBuilder.discoveryDirectory(path).build())
            .assertThat()
            .hasSize(1)
            .first()
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .containsExactly(Stage.ARCHIVE, Step.RUN);
    }

    @Test
    void givenNoExplodedModule_whenExecuteTask_thenNoModuleProduct(Path path) {
        var products = ConveyorTaskBindings.from(
                FakeConveyorSchematicBuilder.discoveryDirectory(path)
                    .build()
            )
            .executeTasks();

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

        var products = ConveyorTaskBindings.from(schematic)
            .executeTasks(schematic.product(explodedModule, ProductType.EXPLODED_MODULE));

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
