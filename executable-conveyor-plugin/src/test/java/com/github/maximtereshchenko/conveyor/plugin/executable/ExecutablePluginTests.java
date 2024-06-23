package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;
import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;
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
    void givenPlugin_whenBindings_thenTaskBindToArchiveFinalize() {
        assertThat(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "classes.directory", "",
                    "main.class", "",
                    "destination", ""
                )
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
    void givenNoClasses_whenExecuteTasks_thenNoExecutable(@TempDir Path path) {
        ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "classes.directory", path.resolve("classes").toString(),
                    "main.class", "",
                    "destination", path.resolve("executable").toString()
                )
            )
        );

        assertThat(path).isEmptyDirectory();
    }

    @Test
    void givenNoDependencies_whenExecuteTasks_thenClassesHaveNoExtractedDependencies(
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));

        ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "classes.directory", classes.toString(),
                    "main.class", "",
                    "destination", path.resolve("executable").toString()
                )
            )
        );

        assertThat(Directories.files(classes)).containsExactly(manifest(classes));
    }

    @Test
    void givenDependency_whenExecuteTasks_thenDependencyIsExtractedToClasses(
        @TempDir Path path
    ) throws IOException {
        var dependencyContainer = Files.createDirectory(path.resolve("dependency-container"));
        Files.createFile(dependencyContainer.resolve("file"));
        var dependency = path.resolve("dependency");
        new ZipArchiveContainer(dependencyContainer).archive(dependency);
        var classes = Files.createDirectory(path.resolve("classes"));

        ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(dependency),
                Map.of(
                    "classes.directory", classes.toString(),
                    "main.class", "",
                    "destination", path.resolve("executable").toString()
                )
            )
        );

        Directories.assertThatDirectoryContentsEqual(
            classes,
            dependencyContainer,
            manifest(classes)
        );
    }

    @Test
    void givenClassesWithDependencies_whenExecuteTasks_thenExecutableExists(
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));
        Files.createFile(classes.resolve("file"));
        var executable = path.resolve("executable");

        ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "classes.directory", classes.toString(),
                    "main.class", "",
                    "destination", executable.toString()
                )
            )
        );

        assertThat(executable).exists();
        var extracted = Files.createDirectory(path.resolve("extracted"));
        new ZipArchive(executable).extract(extracted);
        Directories.assertThatDirectoryContentsEqual(
            extracted,
            classes
        );
    }

    @Test
    void givenMainClass_whenExecuteTasks_thenExecutableContainsManifestWithMainClass(
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));
        var executable = path.resolve("executable");

        ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "classes.directory", classes.toString(),
                    "main.class", "main.Main",
                    "destination", executable.toString()
                )
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
