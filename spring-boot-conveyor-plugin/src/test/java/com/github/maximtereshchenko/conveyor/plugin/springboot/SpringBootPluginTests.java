package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.plugin.api.*;
import com.github.maximtereshchenko.conveyor.plugin.test.Dsl;
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

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class SpringBootPluginTests {

    @Test
    void givenPlugin_whenTasks_thenTaskBindToArchiveFinalize(@TempDir Path path)
        throws IOException {
        var container = path.resolve("container");
        var classes = path.resolve("classes");
        var destination = path.resolve("destination");
        var dependency = path.resolve("dependency");

        new Dsl(new SpringBootPlugin(), path)
            .givenDependency(dependency)
            .givenConfiguration("container.directory", container)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("destination", destination)
            .tasks()
            .contain(
                new ConveyorTask(
                    "copy-classpath",
                    BindingStage.ARCHIVE,
                    BindingStep.FINALIZE,
                    null,
                    Set.of(
                        new PathConveyorTaskInput(classes),
                        new PathConveyorTaskInput(dependency)
                    ),
                    Set.of(new PathConveyorTaskOutput(container.resolve("classpath"))),
                    Cache.ENABLED
                ),
                new ConveyorTask(
                    "extract-spring-boot-launcher",
                    BindingStage.ARCHIVE,
                    BindingStep.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "write-properties",
                    BindingStage.ARCHIVE,
                    BindingStep.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "write-manifest",
                    BindingStage.ARCHIVE,
                    BindingStep.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "archive-executable",
                    BindingStage.ARCHIVE,
                    BindingStep.FINALIZE,
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

        new Dsl(new SpringBootPlugin(), path)
            .givenConfiguration("container.directory", container)
            .givenConfiguration("classes.directory", path.resolve("classes"))
            .givenConfiguration("launched.class", "Main")
            .givenConfiguration("destination", executable)
            .tasks()
            .execute();

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

        new Dsl(new SpringBootPlugin(), path)
            .givenConfiguration("container.directory", container)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("launched.class", "Main")
            .givenConfiguration("destination", executable)
            .tasks()
            .execute();

        assertThat(container.resolve("classpath").resolve("classes"))
            .directoryContentIsEqualTo(classes);
    }

    @Test
    void givenClasses_whenExecuteTasks_thenDependenciesAreCopiedToContainer(
        @TempDir Path path
    ) throws IOException {
        var container = path.resolve("container");
        var classes = Files.createDirectory(path.resolve("classes"));
        Files.createFile(classes.resolve("Dummy.class"));

        new Dsl(new SpringBootPlugin(), path)
            .givenDependency(Files.createFile(path.resolve("dependency")))
            .givenConfiguration("container.directory", container)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("launched.class", "Main")
            .givenConfiguration("destination", path.resolve("executable"))
            .tasks()
            .execute();

        assertThat(container.resolve("classpath").resolve("dependency")).exists();
    }

    @Test
    void givenClasses_whenExecuteTasks_thenSpringBootLauncherIsExtracted(@TempDir Path path)
        throws IOException {
        var container = path.resolve("container");
        var classes = Files.createDirectory(path.resolve("classes"));
        Files.createFile(classes.resolve("Dummy.class"));

        new Dsl(new SpringBootPlugin(), path)
            .givenConfiguration("container.directory", container)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("launched.class", "Main")
            .givenConfiguration("destination", path.resolve("executable"))
            .tasks()
            .execute();

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
        var classes = Files.createDirectory(path.resolve("classes"));
        Files.createFile(classes.resolve("Dummy.class"));

        new Dsl(new SpringBootPlugin(), path)
            .givenConfiguration("container.directory", container)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("launched.class", "Main")
            .givenConfiguration("destination", path.resolve("executable"))
            .tasks()
            .execute();

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
        var classes = Files.createDirectory(path.resolve("classes"));
        Files.createFile(classes.resolve("Dummy.class"));

        new Dsl(new SpringBootPlugin(), path)
            .givenConfiguration("container.directory", container)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("launched.class", "Main")
            .givenConfiguration("destination", path.resolve("executable"))
            .tasks()
            .execute();

        assertThat(container.resolve("META-INF").resolve("MANIFEST.MF"))
            .content()
            .contains("Main-Class: " + Configuration.MAIN_CLASS_NAME);
    }

    @Test
    void givenClasses_whenExecuteTasks_thenExecutableIsCreated(@TempDir Path path)
        throws IOException {
        var container = path.resolve("container");
        var classes = Files.createDirectory(path.resolve("classes"));
        Files.createFile(classes.resolve("Dummy.class"));
        var executable = path.resolve("executable");

        new Dsl(new SpringBootPlugin(), path)
            .givenConfiguration("container.directory", container)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("launched.class", "Main")
            .givenConfiguration("destination", executable)
            .tasks()
            .execute();

        assertThat(executable).exists();
        var extracted = Files.createDirectory(path.resolve("extracted"));
        new ZipArchive(executable).extract(extracted);
        assertThat(extracted).directoryContentIsEqualTo(container);
    }

    private Properties properties(Path path) throws IOException {
        try (var inputStream = Files.newInputStream(path)) {
            var properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }
}
