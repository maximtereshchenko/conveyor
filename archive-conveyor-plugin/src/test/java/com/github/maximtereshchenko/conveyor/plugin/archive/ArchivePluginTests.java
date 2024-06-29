package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
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
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class ArchivePluginTests {

    private final ConveyorPlugin plugin = new ArchivePlugin();

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @Test
    void givenPlugin_whenTasks_thenTaskBindToArchiveRun(@TempDir Path path) throws IOException {
        var classes = path.resolve("classes");
        var destination = path.resolve("destination");

        assertThat(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "classes.directory", classes.toString(),
                    "destination", destination.toString()
                )
            )
        )
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("action")
            .containsExactly(
                new ConveyorTask(
                    "archive",
                    Stage.ARCHIVE,
                    Step.RUN,
                    null,
                    new TreeSet<>(Set.of(classes)),
                    new TreeSet<>(Set.of(destination)),
                    Cache.ENABLED
                ),
                new ConveyorTask(
                    "publish-jar-artifact",
                    Stage.ARCHIVE,
                    Step.FINALIZE,
                    null,
                    new TreeSet<>(),
                    new TreeSet<>(),
                    Cache.DISABLED
                )
            );
    }

    @Test
    void givenNoClasses_whenExecuteTask_thenNoArtifact(@TempDir Path path) throws IOException {
        var archive = path.resolve("archive");

        var artifacts = ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
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
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "classes.directory", classes.toString(),
                    "destination", archive.toString()
                )
            )
        );

        assertThat(artifacts).hasSize(1);
        var extracted = Files.createDirectory(path.resolve("extracted"));
        new ZipArchive(artifacts.getLast()).extract(extracted);
        Directories.assertThatDirectoryContentsEqual(extracted, classes);
    }
}
