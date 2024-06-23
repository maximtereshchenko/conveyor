package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;
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

final class ArchivePluginTests {

    private final ConveyorPlugin plugin = new ArchivePlugin();

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @Test
    void givenPlugin_whenBindings_thenTaskBindToArchiveRun() {
        assertThat(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "classes.directory", "",
                    "destination", ""
                )
            )
        )
            .hasSize(1)
            .first()
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .containsExactly(Stage.ARCHIVE, Step.RUN);
    }

    @Test
    void givenNoClasses_whenExecuteTask_thenNoArtifact(@TempDir Path path) {
        var archive = path.resolve("archive");

        var artifacts = ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "classes.directory", path.resolve("classes").toString(),
                    "destination", archive.toString()
                )
            )
        );

        assertThat(artifacts).isEmpty();
        assertThat(archive).doesNotExist();
    }

    @ParameterizedTest
    @MethodSource("entries")
    void givenClasses_whenExecuteTask_thenArtifactContainsFiles(
        Set<String> entries,
        @TempDir Path path
    ) throws IOException {
        var classes = path.resolve("classes");
        Directories.writeFiles(classes, entries);
        var archive = path.resolve("archive");

        var artifacts = ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of(
                    "classes.directory", classes.toString(),
                    "destination", archive.toString()
                )
            )
        );

        assertThat(artifacts).hasSize(1);
        var extracted = Files.createDirectory(path.resolve("extracted"));
        new ZipArchive(artifacts.getFirst()).extract(extracted);
        Directories.assertThatDirectoryContentsEqual(extracted, classes);
    }
}
