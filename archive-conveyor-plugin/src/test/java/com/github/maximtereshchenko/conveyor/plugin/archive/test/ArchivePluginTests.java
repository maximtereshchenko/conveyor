package com.github.maximtereshchenko.conveyor.plugin.archive.test;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTaskBindings;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.jimfs.JimfsExtension;
import com.github.maximtereshchenko.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JimfsExtension.class)
final class ArchivePluginTests {

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

    @Test
    void givenFile_whenExecuteTask_thenModuleContainsFile(Path path) throws IOException {
        var explodedModule = Files.createDirectory(path.resolve("exploded-module"));
        Files.writeString(explodedModule.resolve("file"), "content");
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
                assertThatDirectoryContentsEquals(extracted, explodedModule);
            });
    }

    @Test
    void givenFileInDirectory_whenExecuteTask_thenModuleContainsFile(Path path)
        throws IOException {
        var explodedModule = Files.createDirectory(path.resolve("exploded-module"));
        Files.writeString(
            Files.createDirectory(explodedModule.resolve("directory")).resolve("file"),
            "content"
        );
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
                assertThatDirectoryContentsEquals(extracted, explodedModule);
            });
    }

    @Test
    void givenMultipleFiles_whenExecuteTask_thenModuleContainsAllFiles(Path path)
        throws IOException {
        var explodedModule = Files.createDirectory(path.resolve("exploded-module"));
        Files.writeString(explodedModule.resolve("first"), "first");
        Files.writeString(explodedModule.resolve("second"), "second");
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
                assertThatDirectoryContentsEquals(extracted, explodedModule);
            });
    }

    @Test
    void givenMultipleFilesInDirectories_whenExecuteTask_thenModuleContainsAllFiles(Path path)
        throws IOException {
        var explodedModule = Files.createDirectory(path.resolve("exploded-module"));
        Files.writeString(explodedModule.resolve("first"), "first");
        Files.writeString(
            Files.createDirectory(explodedModule.resolve("directory")).resolve("second"),
            "second"
        );
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
                assertThatDirectoryContentsEquals(extracted, explodedModule);
            });
    }

    private void assertThatDirectoryContentsEquals(Path actual, Path expected) {
        assertThat(files(actual))
            .zipSatisfy(
                files(expected),
                (actualFile, expectedFile) -> {
                    assertThat(actual.relativize(actualFile))
                        .isEqualTo(expected.relativize(expectedFile));
                    assertThat(actualFile).hasSameTextualContentAs(expectedFile);
                }
            );
    }

    private Set<Path> files(Path path) {
        if (Files.isRegularFile(path)) {
            return Set.of(path);
        }
        try (var stream = Files.list(path)) {
            return stream.map(this::files)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
