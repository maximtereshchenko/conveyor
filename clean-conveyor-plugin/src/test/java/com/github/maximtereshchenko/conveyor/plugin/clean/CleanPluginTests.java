package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

final class CleanPluginTests {

    private final ConveyorPlugin plugin = new CleanPlugin();

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @Test
    void givenPlugin_whenTasks_thenTaskBindToCleanRun(@TempDir Path path) throws IOException {
        assertThat(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of("directory", path.toString())
            )
        )
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("action")
            .containsExactly(
                new ConveyorTask(
                    "clean",
                    Stage.CLEAN,
                    Step.RUN,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                )
            );
    }

    @Test
    void givenNoDirectory_whenExecuteTasks_thenTaskDidNotFail(@TempDir Path path)
        throws IOException {
        var tasks = plugin.tasks(
            FakeConveyorSchematic.from(path),
            Map.of("directory", path.resolve("does-not-exist").toString())
        );

        assertThatCode(() -> ConveyorTasks.executeTasks(tasks)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("entries")
    void givenDirectory_whenExecuteTasks_thenDirectoryIsDeleted(
        Set<String> entries,
        @TempDir Path path
    ) throws IOException {
        Directories.writeFiles(path, entries);

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of("directory", path.toString())
            )
        );

        assertThat(path).doesNotExist();
    }
}