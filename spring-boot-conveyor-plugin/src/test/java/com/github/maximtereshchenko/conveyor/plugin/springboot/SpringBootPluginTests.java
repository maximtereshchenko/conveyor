package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;
import com.github.maximtereshchenko.test.common.Directories;
import com.github.maximtereshchenko.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

final class SpringBootPluginTests {

    private final ConveyorPlugin plugin = new SpringBootPlugin();

    @Test
    void givenPlugin_whenBindings_thenTaskBindToArchiveFinalize(@TempDir Path path) {
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
                tuple(Stage.ARCHIVE, Step.FINALIZE),
                tuple(Stage.ARCHIVE, Step.FINALIZE),
                tuple(Stage.ARCHIVE, Step.FINALIZE)
            );
    }

    @Test
    void givenNoExplodedJar_whenExecuteTasks_thenNoJarCreated(@TempDir Path path) {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .constructionDirectory(path.resolve("construction"))
            .build();

        ConveyorTasks.executeTasks(plugin.bindings(schematic, Map.of()));

        assertThat(schematic.constructionDirectory()).doesNotExist();
    }

    @Test
    void givenExplodedJar_whenExecuteTasks_thenExplodedJarIsCopiedToContainer(@TempDir Path path)
        throws IOException {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .constructionDirectory(path.resolve("construction"))
            .build();
        var explodedJar = Files.createDirectory(path.resolve("exploded-jar"));
        Files.createFile(explodedJar.resolve("file"));

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of("launched-class", "example.Main")),
            new Product(schematic.coordinates(), explodedJar, ProductType.EXPLODED_JAR)
        );

        Directories.assertThatDirectoryContentsEqual(
            schematic.constructionDirectory()
                .resolve("executable-container")
                .resolve("class-path")
                .resolve("exploded-jar"),
            explodedJar
        );
    }

    @Test
    void givenExplodedJar_whenExecuteTasks_thenDependenciesAreCopiedToContainer(@TempDir Path path)
        throws IOException {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .constructionDirectory(path.resolve("construction"))
            .dependency(Files.createFile(path.resolve("dependency.jar")))
            .build();

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of("launched-class", "example.Main")),
            new Product(
                schematic.coordinates(),
                Files.createDirectory(path.resolve("exploded-jar")),
                ProductType.EXPLODED_JAR
            )
        );

        assertThat(
            schematic.constructionDirectory()
                .resolve("executable-container")
                .resolve("class-path")
                .resolve("dependency.jar")
        )
            .exists();
    }

    @Test
    void givenExplodedJar_whenExecuteTasks_thenSpringBootLauncherIsExtracted(@TempDir Path path)
        throws IOException {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .constructionDirectory(path.resolve("construction"))
            .build();

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of("launched-class", "example.Main")),
            new Product(
                schematic.coordinates(),
                Files.createDirectory(path.resolve("exploded-jar")),
                ProductType.EXPLODED_JAR
            )
        );

        assertThat(
            schematic.constructionDirectory()
                .resolve("executable-container")
                .resolve("com")
                .resolve("github")
                .resolve("maximtereshchenko")
                .resolve("conveyor")
                .resolve("springboot")
                .resolve("SpringBootLauncher.class")
        )
            .exists();
    }

    @Test
    void givenExplodedJar_whenExecuteTasks_thenPropertiesAreWritten(@TempDir Path path)
        throws IOException {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .constructionDirectory(path.resolve("construction"))
            .build();

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of("launched-class", "example.Main")),
            new Product(
                schematic.coordinates(),
                Files.createDirectory(path.resolve("exploded-jar")),
                ProductType.EXPLODED_JAR
            )
        );

        assertThat(
            properties(
                schematic.constructionDirectory()
                    .resolve("executable-container")
                    .resolve(Configuration.PROPERTIES_CLASS_PATH_LOCATION)
            )
        )
            .containsOnly(
                Map.entry(Configuration.CLASS_PATH_DIRECTORY_KEY, "class-path"),
                Map.entry(Configuration.LAUNCHED_CLASS_NAME_KEY, "example.Main")
            );
    }

    @Test
    void givenExplodedJar_whenExecuteTasks_thenManifestIsWritten(@TempDir Path path)
        throws IOException {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .constructionDirectory(path.resolve("construction"))
            .build();

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of("launched-class", "example.Main")),
            new Product(
                schematic.coordinates(),
                Files.createDirectory(path.resolve("exploded-jar")),
                ProductType.EXPLODED_JAR
            )
        );

        assertThat(
            schematic.constructionDirectory()
                .resolve("executable-container")
                .resolve("META-INF")
                .resolve("MANIFEST.MF")
        )
            .content()
            .contains("Main-Class: " + Configuration.MAIN_CLASS_NAME);
    }

    @Test
    void givenExplodedJar_whenExecuteTasks_thenJarIsCreated(@TempDir Path path)
        throws IOException {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path)
            .constructionDirectory(path.resolve("construction"))
            .build();

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of("launched-class", "example.Main")),
            new Product(
                schematic.coordinates(),
                Files.createDirectory(path.resolve("exploded-jar")),
                ProductType.EXPLODED_JAR
            )
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
            schematic.constructionDirectory().resolve("executable-container")
        );
    }

    private Properties properties(Path path) throws IOException {
        try (var inputStream = Files.newInputStream(path)) {
            var properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }
}
