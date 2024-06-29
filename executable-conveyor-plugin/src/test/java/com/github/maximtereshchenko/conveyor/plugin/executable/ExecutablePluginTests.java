package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
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
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

final class ExecutablePluginTests {

    private final ConveyorPlugin plugin = new ExecutablePlugin();

    @Test
    void givenPlugin_whenTasks_thenTaskBindToArchiveFinalize(@TempDir Path path)
        throws IOException {
        var classes = path.resolve("classes");
        var destination = path.resolve("destination");

        assertThat(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "classes.directory", classes.toString(),
                    "main.class", "",
                    "destination", destination.toString()
                )
            )
        )
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("action")
            .containsExactly(
                new ConveyorTask(
                    "extract-dependencies",
                    Stage.ARCHIVE,
                    Step.FINALIZE,
                    null,
                    new TreeSet<>(),
                    new TreeSet<>(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "write-manifest",
                    Stage.ARCHIVE,
                    Step.FINALIZE,
                    null,
                    new TreeSet<>(),
                    new TreeSet<>(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "archive-executable",
                    Stage.ARCHIVE,
                    Step.FINALIZE,
                    null,
                    new TreeSet<>(Set.of(classes)),
                    new TreeSet<>(Set.of(destination)),
                    Cache.ENABLED
                )
            );
    }

    @Test
    void givenNoClasses_whenExecuteTasks_thenNoExecutable(@TempDir Path path) throws IOException {
        var classes = path.resolve("classes");
        var executable = path.resolve("executable");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "classes.directory", classes.toString(),
                    "main.class", "",
                    "destination", executable.toString()
                )
            )
        );

        assertThat(classes).doesNotExist();
        assertThat(executable).doesNotExist();
    }

    @Test
    void givenNoDependencies_whenExecuteTasks_thenClassesHaveNoExtractedDependencies(
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
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
            plugin.tasks(
                FakeConveyorSchematic.from(path, dependency),
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
            plugin.tasks(
                FakeConveyorSchematic.from(path),
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
            plugin.tasks(
                FakeConveyorSchematic.from(path),
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
