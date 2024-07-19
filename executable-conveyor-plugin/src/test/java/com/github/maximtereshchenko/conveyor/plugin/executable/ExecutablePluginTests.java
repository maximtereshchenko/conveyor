package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.*;
import com.github.maximtereshchenko.conveyor.plugin.test.Dsl;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;
import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class ExecutablePluginTests {

    @Test
    void givenPlugin_whenTasks_thenTaskBindToArchiveFinalize(@TempDir Path path)
        throws IOException {
        var container = path.resolve("container");
        var dependency = path.resolve("dependency");

        new Dsl(new ExecutablePlugin(), path)
            .givenDependency(dependency)
            .givenConfiguration("classes.directory", path.resolve("classes"))
            .givenConfiguration("container.directory", container)
            .givenConfiguration("main.class")
            .givenConfiguration("destination", path.resolve("destination"))
            .tasks()
            .contain(
                new ConveyorTask(
                    "extract-dependencies",
                    BindingStage.ARCHIVE,
                    BindingStep.FINALIZE,
                    null,
                    Set.of(new PathConveyorTaskInput(dependency)),
                    Set.of(new PathConveyorTaskOutput(container)),
                    Cache.ENABLED
                ),
                new ConveyorTask(
                    "copy-classes",
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
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                )
            );
    }

    @Test
    void givenNoDependencies_whenExecuteTasks_thenClassesHaveNoExtractedDependencies(
        @TempDir Path path
    ) throws IOException {
        var container = path.resolve("container");

        new Dsl(new ExecutablePlugin(), path)
            .givenConfiguration("classes.directory", path.resolve("classes"))
            .givenConfiguration("container.directory", path.resolve("container"))
            .givenConfiguration("main.class")
            .givenConfiguration("destination", path.resolve("executable"))
            .tasks()
            .execute();

        assertThat(new FileTree(container).files()).containsExactly(manifest(container));
    }

    @Test
    void givenDependency_whenExecuteTasks_thenDependencyIsExtractedToContainer(
        @TempDir Path path
    ) throws IOException {
        var dependencyContainer = Files.createDirectory(path.resolve("dependency-container"));
        Files.createFile(dependencyContainer.resolve("file"));
        var dependency = path.resolve("dependency");
        new ZipArchiveContainer(dependencyContainer).archive(dependency);
        var container = path.resolve("container");

        new Dsl(new ExecutablePlugin(), path)
            .givenDependency(dependency)
            .givenConfiguration("classes.directory", path.resolve("classes"))
            .givenConfiguration("container.directory", container)
            .givenConfiguration("main.class")
            .givenConfiguration("destination", path.resolve("executable"))
            .tasks()
            .execute();

        assertThat(container)
            .directoryContentIsEqualToIgnoring(dependencyContainer, manifest(container));
    }

    @Test
    void givenDependencyWithModuleInfo_whenExecuteTasks_thenModuleInfoIsNotExtractedToClasses(
        @TempDir Path path
    ) throws IOException {
        var dependencyContainer = Files.createDirectory(path.resolve("dependency-container"));
        Files.createFile(dependencyContainer.resolve("module-info.class"));
        var dependency = path.resolve("dependency");
        new ZipArchiveContainer(dependencyContainer).archive(dependency);
        var container = path.resolve("container");

        new Dsl(new ExecutablePlugin(), path)
            .givenDependency(dependency)
            .givenConfiguration("classes.directory", path.resolve("classes"))
            .givenConfiguration("container.directory", container)
            .givenConfiguration("main.class")
            .givenConfiguration("destination", path.resolve("executable"))
            .tasks()
            .execute();

        assertThat(container.resolve("module-info.class")).doesNotExist();
    }

    @Test
    void givenClassesWithDependencies_whenExecuteTasks_thenExecutableExists(
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));
        Files.createFile(classes.resolve("file"));
        var executable = path.resolve("executable");

        new Dsl(new ExecutablePlugin(), path)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("container.directory", path.resolve("container"))
            .givenConfiguration("main.class")
            .givenConfiguration("destination", executable)
            .tasks()
            .execute();

        assertThat(executable).exists();
        var extracted = Files.createDirectory(path.resolve("extracted"));
        new ZipArchive(executable).extract(extracted);
        assertThat(extracted).directoryContentIsEqualToIgnoring(classes, manifest(extracted));
    }

    @Test
    void givenMainClass_whenExecuteTasks_thenExecutableContainsManifestWithMainClass(
        @TempDir Path path
    ) throws IOException {
        var executable = path.resolve("executable");

        new Dsl(new ExecutablePlugin(), path)
            .givenConfiguration("classes.directory", path.resolve("classes"))
            .givenConfiguration("container.directory", path.resolve("container"))
            .givenConfiguration("main.class", "main.Main")
            .givenConfiguration("destination", executable)
            .tasks()
            .execute();

        assertThat(executable).exists();
        var extracted = Files.createDirectory(path.resolve("extracted"));
        new ZipArchive(executable).extract(extracted);
        assertThat(manifest(extracted))
            .content()
            .contains("Main-Class: main.Main");
    }

    @Test
    void givenNoConfiguration_whenExecuteTasks_thenDefaultPathsAreUsed(@TempDir Path path)
        throws IOException {
        var conveyor = path.resolve(".conveyor");
        var classes = Files.createDirectories(conveyor.resolve("classes"));
        Files.createFile(classes.resolve("Dummy.class"));

        new Dsl(new ExecutablePlugin(), path)
            .givenConfiguration("main.class", "Dummy")
            .tasks()
            .execute();

        var extracted = Files.createDirectory(path.resolve("extracted"));
        new ZipArchive(conveyor.resolve("project-1.0.0-executable.jar")).extract(extracted);
        assertThat(extracted).directoryContentIsEqualTo(conveyor.resolve("executable-container"));
    }

    private Path manifest(Path path) {
        return path.resolve("META-INF").resolve("MANIFEST.MF");
    }
}
