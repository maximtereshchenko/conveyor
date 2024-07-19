package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.common.test.DirectoryEntriesSource;
import com.github.maximtereshchenko.conveyor.plugin.api.BindingStage;
import com.github.maximtereshchenko.conveyor.plugin.api.BindingStep;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.test.Dsl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class CopyResourcesTests {

    @Test
    void givenPlugin_whenBindings_thenCopyResourcesBindingReturned(@TempDir Path path)
        throws IOException {
        new Dsl(new ResourcesPlugin(), path)
            .givenConfiguration("resources.directory")
            .givenConfiguration("resources.destination.directory")
            .givenConfiguration("test.resources.directory")
            .givenConfiguration("test.resources.destination.directory")
            .tasks()
            .contain(
                new ConveyorTask(
                    "copy-resources",
                    BindingStage.COMPILE,
                    BindingStep.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "copy-test-resources",
                    BindingStage.TEST,
                    BindingStep.PREPARE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                )
            );
    }

    @Test
    void givenNoResources_whenExecuteTasks_thenClassesDirectoryIsEmpty(
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));
        var resources = path.resolve("resources");

        new Dsl(new ResourcesPlugin(), path)
            .givenConfiguration("resources.directory", resources)
            .givenConfiguration("resources.destination.directory", classes)
            .givenConfiguration("test.resources.directory", resources)
            .givenConfiguration("test.resources.destination.directory", classes)
            .tasks()
            .execute();

        assertThat(classes).isEmptyDirectory();
    }

    @Test
    void givenResourcesAlreadyCopied_whenExecuteTasks_thenNoExceptionThrown(
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));
        var resources = Files.createDirectory(path.resolve("resources"));
        Files.copy(Files.createFile(resources.resolve("resource")), classes.resolve("resource"));

        new Dsl(new ResourcesPlugin(), path)
            .givenConfiguration("resources.directory", resources)
            .givenConfiguration("resources.destination.directory", classes)
            .givenConfiguration("test.resources.directory", resources)
            .givenConfiguration("test.resources.destination.directory", classes)
            .tasks()
            .execute()
            .thenNoException();
    }

    @ParameterizedTest
    @DirectoryEntriesSource
    void givenResources_whenExecuteTasks_thenClassesContainResources(
        Path directory,
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));
        var testClasses = Files.createDirectory(path.resolve("testClasses"));

        new Dsl(new ResourcesPlugin(), path)
            .givenConfiguration("resources.directory", directory)
            .givenConfiguration("resources.destination.directory", classes)
            .givenConfiguration("test.resources.directory", directory)
            .givenConfiguration("test.resources.destination.directory", testClasses)
            .tasks()
            .execute();

        assertThat(classes).directoryContentIsEqualTo(directory);
        assertThat(testClasses).directoryContentIsEqualTo(directory);
    }

    @Test
    void givenNoConfiguration_whenExecuteTasks_thenResourcesCopiedFromDefaultDirectoryToDefaultDestination(
        @TempDir Path path
    ) throws IOException {
        var resources = Files.createDirectories(
            path.resolve("src").resolve("main").resolve("resources")
        );
        Files.createFile(resources.resolve("resource"));
        var testResources = Files.createDirectories(
            path.resolve("src").resolve("test").resolve("resources")
        );
        Files.createFile(testResources.resolve("test-resource"));

        new Dsl(new ResourcesPlugin(), path)
            .tasks()
            .execute();

        var conveyor = path.resolve(".conveyor");
        assertThat(conveyor.resolve("classes")).directoryContentIsEqualTo(resources);
        assertThat(conveyor.resolve("test-classes")).directoryContentIsEqualTo(testResources);
    }
}
