package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.api.PathConveyorTaskInput;
import com.github.maximtereshchenko.conveyor.plugin.api.PathConveyorTaskOutput;
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
        var classes = path.resolve("classes");
        var destination = path.resolve("destination");
        var dependency = path.resolve("dependency");

        new Dsl(new ExecutablePlugin(), path)
            .givenDependency(dependency)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("main.class")
            .givenConfiguration("destination", destination)
            .tasks()
            .contain(
                new ConveyorTask(
                    "extract-dependencies",
                    Stage.ARCHIVE,
                    Step.FINALIZE,
                    null,
                    Set.of(
                        new PathConveyorTaskInput(classes),
                        new PathConveyorTaskInput(dependency)
                    ),
                    Set.of(new PathConveyorTaskOutput(classes)),
                    Cache.ENABLED
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
                    Set.of(new PathConveyorTaskInput(classes)),
                    Set.of(new PathConveyorTaskOutput(destination)),
                    Cache.ENABLED
                )
            );
    }

    @Test
    void givenNoClasses_whenExecuteTasks_thenNoExecutable(@TempDir Path path) throws IOException {
        var classes = path.resolve("classes");
        var executable = path.resolve("executable");

        new Dsl(new ExecutablePlugin(), path)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("main.class")
            .givenConfiguration("destination", executable)
            .tasks()
            .execute();

        assertThat(classes).doesNotExist();
        assertThat(executable).doesNotExist();
    }

    @Test
    void givenNoDependencies_whenExecuteTasks_thenClassesHaveNoExtractedDependencies(
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));

        new Dsl(new ExecutablePlugin(), path)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("main.class")
            .givenConfiguration("destination", path.resolve("executable"))
            .tasks()
            .execute();

        assertThat(new FileTree(classes).files()).containsExactly(manifest(classes));
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

        new Dsl(new ExecutablePlugin(), path)
            .givenDependency(dependency)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("main.class")
            .givenConfiguration("destination", path.resolve("executable"))
            .tasks()
            .execute();

        assertThat(classes)
            .directoryContentIsEqualToIgnoring(dependencyContainer, manifest(classes));
    }

    @Test
    void givenDependencyWithModuleInfo_whenExecuteTasks_thenModuleInfoIsNotExtractedToClasses(
        @TempDir Path path
    ) throws IOException {
        var dependencyContainer = Files.createDirectory(path.resolve("dependency-container"));
        Files.createFile(dependencyContainer.resolve("module-info.class"));
        var dependency = path.resolve("dependency");
        new ZipArchiveContainer(dependencyContainer).archive(dependency);
        var classes = Files.createDirectory(path.resolve("classes"));

        new Dsl(new ExecutablePlugin(), path)
            .givenDependency(dependency)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("main.class")
            .givenConfiguration("destination", path.resolve("executable"))
            .tasks()
            .execute();

        assertThat(classes.resolve("module-info.class")).doesNotExist();
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
            .givenConfiguration("main.class")
            .givenConfiguration("destination", executable)
            .tasks()
            .execute();

        assertThat(executable).exists();
        var extracted = Files.createDirectory(path.resolve("extracted"));
        new ZipArchive(executable).extract(extracted);
        assertThat(extracted).directoryContentIsEqualTo(classes);
    }

    @Test
    void givenMainClass_whenExecuteTasks_thenExecutableContainsManifestWithMainClass(
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));
        var executable = path.resolve("executable");

        new Dsl(new ExecutablePlugin(), path)
            .givenConfiguration("classes.directory", classes)
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

    private Path manifest(Path path) {
        return path.resolve("META-INF").resolve("MANIFEST.MF");
    }
}
