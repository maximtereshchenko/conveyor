package com.github.maximtereshchenko.conveyor.plugin.executable.test;

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.executable.ExecutablePlugin;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.test.common.Directories;
import com.github.maximtereshchenko.zip.ZipArchive;
import com.github.maximtereshchenko.zip.ZipArchiveContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

final class ExecutablePluginTests {

    private final ConveyorPlugin plugin = new ExecutablePlugin();

    @Test
    void givenPlugin_whenBindings_thenTaskBindToArchiveRun(@TempDir Path path) {
        assertThat(
            plugin.bindings(
                FakeConveyorSchematicBuilder.discoveryDirectory(path).build(),
                Map.of()
            )
        )
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .containsExactly(
                tuple(Stage.ARCHIVE, Step.FINALIZE),
                tuple(Stage.ARCHIVE, Step.FINALIZE),
                tuple(Stage.ARCHIVE, Step.FINALIZE)
            );
    }

    @Test
    void givenNoExplodedJar_whenExecuteTasks_thenNoExecutableJar(@TempDir Path path)
        throws IOException {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .constructionDirectory(Files.createDirectories(path.resolve("construction")))
            .build();

        ConveyorTasks.executeTasks(plugin.bindings(schematic, Map.of()));

        assertThat(schematic.constructionDirectory()).isEmptyDirectory();
    }

    @Test
    void givenNoDependencies_whenExecuteTasks_thenExplodedJarHasNoExtractedDependencies(@TempDir Path path)
        throws IOException {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();
        var explodedJar = Files.createDirectory(path.resolve("exploded-jar"));

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of()),
            new Product(schematic.coordinates(), explodedJar, ProductType.EXPLODED_JAR)
        );

        assertThat(Directories.files(explodedJar)).containsExactly(manifest(explodedJar));
    }

    @Test
    void givenDependency_whenExecuteTasks_thenDependencyIsExtractedToExplodedJar(
        @TempDir Path path
    ) throws IOException {
        var dependencyContainer = Files.createDirectory(path.resolve("dependency-container"));
        Files.createFile(dependencyContainer.resolve("file"));
        var dependency = path.resolve("dependency");
        new ZipArchiveContainer(dependencyContainer).archive(dependency);
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .dependency(dependency)
            .build();
        var explodedJar = Files.createDirectory(path.resolve("exploded-jar"));

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of()),
            new Product(schematic.coordinates(), explodedJar, ProductType.EXPLODED_JAR)
        );

        Directories.assertThatDirectoryContentsEqual(
            explodedJar,
            dependencyContainer,
            manifest(explodedJar)
        );
    }

    @Test
    void givenProductsFromOtherSchematics_whenExecuteTasks_thenDependenciesAreCopiedForCurrentSchematic(
        @TempDir Path path
    ) throws IOException {
        var dependencyContainer = Files.createDirectory(path.resolve("dependency-container"));
        Files.createFile(dependencyContainer.resolve("file"));
        var dependency = path.resolve("dependency");
        new ZipArchiveContainer(dependencyContainer).archive(dependency);
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .dependency(dependency)
            .build();
        var explodedJar = Files.createDirectory(path.resolve("exploded-jar"));
        var otherExplodedJar = path.resolve("other-exploded-jar");

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of()),
            new Product(
                new SchematicCoordinates("group", "other-schematic", "1.0.0"),
                otherExplodedJar,
                ProductType.EXPLODED_JAR
            ),
            new Product(schematic.coordinates(), explodedJar, ProductType.EXPLODED_JAR)
        );

        assertThat(otherExplodedJar).doesNotExist();
        assertThat(explodedJar).isNotEmptyDirectory();
    }

    @Test
    void givenExplodedJarWithDependencies_whenExecuteTasks_thenExecutableJarExists(
        @TempDir Path path
    ) throws IOException {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .constructionDirectory(path.resolve("construction"))
            .build();
        var explodedJar = Files.createDirectory(path.resolve("exploded-jar"));
        Files.createFile(explodedJar.resolve("file"));

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of()),
            new Product(schematic.coordinates(), explodedJar, ProductType.EXPLODED_JAR)
        );

        var executable = schematic.constructionDirectory()
            .resolve(
                "%s-%s-executable.jar".formatted(
                    schematic.coordinates().name(),
                    schematic.coordinates().version()
                )
            );
        assertThat(executable).exists();
        var extracted = Files.createDirectory(path.resolve("extracted"));
        new ZipArchive(executable).extract(extracted);
        Directories.assertThatDirectoryContentsEqual(
            extracted,
            explodedJar
        );
    }

    @Test
    void givenExecutableJar_whenExecuteTasks_thenExecutableJarContainsManifestWithMainClass(
        @TempDir Path path
    ) throws IOException {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .constructionDirectory(path.resolve("construction"))
            .build();
        var explodedJar = Files.createDirectory(path.resolve("exploded-jar"));

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of("main-class", "main.Main")),
            new Product(schematic.coordinates(), explodedJar, ProductType.EXPLODED_JAR)
        );

        var executable = schematic.constructionDirectory()
            .resolve(
                "%s-%s-executable.jar".formatted(
                    schematic.coordinates().name(),
                    schematic.coordinates().version()
                )
            );
        assertThat(executable).exists();
        var extracted = Files.createDirectory(path.resolve("extracted"));
        new ZipArchive(executable).extract(extracted);
        assertThat(manifest(extracted))
            .content()
            .contains("Main-Class: main.Main");
    }

    private Path manifest(Path path) {
        return path.resolve("META-INF").resolve("MANIFEST.MF");
    }
}
