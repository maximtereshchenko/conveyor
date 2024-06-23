package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

final class CopyResourcesTests {

    private final ConveyorPlugin plugin = new ResourcesPlugin();

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @Test
    void givenPlugin_whenBindings_thenCopyResourcesBindingReturned() {
        assertThat(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "resources.directory", "",
                    "classes.directory", "",
                    "test.resources.directory", "",
                    "test.classes.directory", ""
                )
            )
        )
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(tuple(Stage.COMPILE, Step.FINALIZE), tuple(Stage.TEST, Step.PREPARE));
    }

    @Test
    void givenNoResources_whenExecuteTasks_thenClassesDirectoryIsEmpty(
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));
        var resources = path.resolve("resources");
        ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "resources.directory", resources.toString(),
                    "classes.directory", classes.toString(),
                    "test.resources.directory", resources.toString(),
                    "test.classes.directory", classes.toString()
                )
            )
        );

        assertThat(classes).isEmptyDirectory();
    }

    @Test
    void givenNoClasses_whenExecuteTasks_thenResourcesAreNotCopied(
        @TempDir Path path
    ) throws IOException {
        var classes = path.resolve("classes");
        var resources = Files.createDirectory(path.resolve("resources"));
        Files.createFile(resources.resolve("resource"));

        ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "resources.directory", resources.toString(),
                    "classes.directory", classes.toString(),
                    "test.resources.directory", resources.toString(),
                    "test.classes.directory", classes.toString()
                )
            )
        );

        assertThat(classes).doesNotExist();
    }

    @ParameterizedTest
    @MethodSource("entries")
    void givenResources_whenExecuteTasks_thenClassesContainResources(
        Set<String> entries,
        @TempDir Path path
    ) throws IOException {
        var classes = Files.createDirectory(path.resolve("classes"));
        var resources = Directories.writeFiles(path.resolve("resources"), entries);
        var testClasses = Files.createDirectory(path.resolve("testClasses"));
        var testResources = Directories.writeFiles(
            path.resolve("testResources"),
            entries
        );

        ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "resources.directory", resources.toString(),
                    "classes.directory", classes.toString(),
                    "test.resources.directory", testResources.toString(),
                    "test.classes.directory", testClasses.toString()
                )
            )
        );

        Directories.assertThatDirectoryContentsEqual(classes, resources);
        Directories.assertThatDirectoryContentsEqual(testClasses, testResources);
    }
}
