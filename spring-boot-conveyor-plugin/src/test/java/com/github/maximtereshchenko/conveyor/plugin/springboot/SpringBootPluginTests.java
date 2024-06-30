package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.plugin.api.*;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

final class SpringBootPluginTests {

    private final ConveyorPlugin plugin = new SpringBootPlugin();

    @Test
    void givenPlugin_whenTasks_thenTaskBindToArchiveFinalize(@TempDir Path path)
        throws IOException {
        var container = path.resolve("container");
        var destination = path.resolve("destination");

        assertThat(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "container.directory", container.toString(),
                    "classes.directory", path.resolve("classes").toString(),
                    "destination", destination.toString()
                )
            )
        )
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("action")
            .containsExactly(
                new ConveyorTask(
                    "copy-dependencies",
                    Stage.ARCHIVE,
                    Step.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "extract-spring-boot-launcher",
                    Stage.ARCHIVE,
                    Step.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "write-properties",
                    Stage.ARCHIVE,
                    Step.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "write-manifest",
                    Stage.ARCHIVE,
                    Step.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "archive-executable",
                    Stage.ARCHIVE,
                    Step.FINALIZE,
                    null,
                    Set.of(new PathConveyorTaskInput(container)),
                    Set.of(new PathConveyorTaskOutput(destination)),
                    Cache.ENABLED
                )
            );
    }

    @Test
    void givenNoClasses_whenExecuteTasks_thenNoExecutable(@TempDir Path path) throws IOException {
        var container = path.resolve("container");
        var executable = path.resolve("executable");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "container.directory", container.toString(),
                    "classes.directory", path.resolve("classes").toString(),
                    "launched.class", "Main",
                    "destination", executable.toString()
                )
            )
        );

        assertThat(container).doesNotExist();
        assertThat(executable).doesNotExist();
    }

    @Test
    void givenClasses_whenExecuteTasks_thenClassesAreCopiedToContainer(@TempDir Path path)
        throws IOException {
        var container = path.resolve("container");
        var classes = Files.createDirectory(path.resolve("classes"));
        Files.createFile(classes.resolve("file"));
        var executable = path.resolve("executable");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "container.directory", container.toString(),
                    "classes.directory", classes.toString(),
                    "launched.class", "Main",
                    "destination", executable.toString()
                )
            )
        );

        Directories.assertThatDirectoryContentsEqual(
            container.resolve("classpath").resolve("classes"),
            classes
        );
    }

    @Test
    void givenClasses_whenExecuteTasks_thenDependenciesAreCopiedToContainer(
        @TempDir Path path
    ) throws IOException {
        var container = path.resolve("container");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path, Files.createFile(path.resolve("dependency"))),
                Map.of(
                    "container.directory", container.toString(),
                    "classes.directory",
                    Files.createDirectory(path.resolve("classes")).toString(),
                    "launched.class", "Main",
                    "destination", path.resolve("executable").toString()
                )
            )
        );

        assertThat(container.resolve("classpath").resolve("dependency")).exists();
    }

    @Test
    void givenClasses_whenExecuteTasks_thenSpringBootLauncherIsExtracted(@TempDir Path path)
        throws IOException {
        var container = path.resolve("container");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "container.directory", container.toString(),
                    "classes.directory",
                    Files.createDirectory(path.resolve("classes")).toString(),
                    "launched.class", "Main",
                    "destination", path.resolve("executable").toString()
                )
            )
        );

        assertThat(
            container.resolve("com")
                .resolve("github")
                .resolve("maximtereshchenko")
                .resolve("conveyor")
                .resolve("springboot")
                .resolve("SpringBootLauncher.class")
        )
            .exists();
    }

    @Test
    void givenClasses_whenExecuteTasks_thenPropertiesAreWritten(@TempDir Path path)
        throws IOException {
        var container = path.resolve("container");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "container.directory", container.toString(),
                    "classes.directory",
                    Files.createDirectory(path.resolve("classes")).toString(),
                    "launched.class", "Main",
                    "destination", path.resolve("executable").toString()
                )
            )
        );

        assertThat(properties(container.resolve(Configuration.PROPERTIES_CLASS_PATH_LOCATION)))
            .containsOnly(
                Map.entry(Configuration.CLASS_PATH_DIRECTORY_KEY, "classpath"),
                Map.entry(Configuration.LAUNCHED_CLASS_NAME_KEY, "Main")
            );
    }

    @Test
    void givenClasses_whenExecuteTasks_thenManifestIsWritten(@TempDir Path path)
        throws IOException {
        var container = path.resolve("container");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "container.directory", container.toString(),
                    "classes.directory",
                    Files.createDirectory(path.resolve("classes")).toString(),
                    "launched.class", "Main",
                    "destination", path.resolve("executable").toString()
                )
            )
        );
        assertThat(container.resolve("META-INF").resolve("MANIFEST.MF"))
            .content()
            .contains("Main-Class: " + Configuration.MAIN_CLASS_NAME);
    }

    @Test
    void givenClasses_whenExecuteTasks_thenExecutableIsCreated(@TempDir Path path)
        throws IOException {
        var container = path.resolve("container");
        var executable = path.resolve("executable");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "container.directory", container.toString(),
                    "classes.directory",
                    Files.createDirectory(path.resolve("classes")).toString(),
                    "launched.class", "Main",
                    "destination", executable.toString()
                )
            )
        );

        assertThat(executable).exists();
        var extracted = Files.createDirectory(path.resolve("extracted"));
        new ZipArchive(executable).extract(extracted);
        Directories.assertThatDirectoryContentsEqual(extracted, container);
    }

    private Properties properties(Path path) throws IOException {
        try (var inputStream = Files.newInputStream(path)) {
            var properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }
}
