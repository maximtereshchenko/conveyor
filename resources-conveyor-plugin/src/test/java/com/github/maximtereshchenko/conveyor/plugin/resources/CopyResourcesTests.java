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
            .givenConfiguration("classes.directory")
            .givenConfiguration("test.resources.directory")
            .givenConfiguration("test.classes.directory")
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
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("test.resources.directory", resources)
            .givenConfiguration("test.classes.directory", classes)
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
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("test.resources.directory", resources)
            .givenConfiguration("test.classes.directory", classes)
            .tasks()
            .execute()
            .thenNoException();
    }

    @Test
    void givenNoClasses_whenExecuteTasks_thenResourcesAreNotCopied(
        @TempDir Path path
    ) throws IOException {
        var classes = path.resolve("classes");
        var resources = Files.createDirectory(path.resolve("resources"));
        Files.createFile(resources.resolve("resource"));

        new Dsl(new ResourcesPlugin(), path)
            .givenConfiguration("resources.directory", resources)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("test.resources.directory", resources)
            .givenConfiguration("test.classes.directory", classes)
            .tasks()
            .execute();

        assertThat(classes).doesNotExist();
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
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("test.resources.directory", directory)
            .givenConfiguration("test.classes.directory", testClasses)
            .tasks()
            .execute();

        assertThat(classes).directoryContentIsEqualTo(directory);
        assertThat(testClasses).directoryContentIsEqualTo(directory);
    }
}
