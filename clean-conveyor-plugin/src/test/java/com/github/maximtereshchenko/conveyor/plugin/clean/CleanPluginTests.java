package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.common.test.DirectoryEntriesSource;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;
import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThatCode;

final class CleanPluginTests {

    private final ConveyorPlugin plugin = new CleanPlugin();

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
    @DirectoryEntriesSource
    void givenDirectory_whenExecuteTasks_thenDirectoryIsDeleted(Path directory) throws IOException {
        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(directory),
                Map.of("directory", directory.toString())
            )
        );

        assertThat(directory).doesNotExist();
    }
}