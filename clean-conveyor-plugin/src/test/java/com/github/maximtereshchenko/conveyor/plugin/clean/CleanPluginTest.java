package com.github.maximtereshchenko.conveyor.plugin.clean;

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
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

final class CleanPluginTest {

    private final ConveyorPlugin plugin = new CleanPlugin();

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @Test
    void givenPlugin_whenBindings_thenTaskBindToCleanRun() {
        assertThat(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of("directory", "")
            )
        )
            .hasSize(1)
            .first()
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .containsExactly(Stage.CLEAN, Step.RUN);
    }

    @Test
    void givenNoDirectory_whenExecuteTasks_thenTaskDidNotFail(@TempDir Path path) {
        var bindings = plugin.bindings(
            new FakeConveyorSchematic(),
            Map.of("directory", path.toString())
        );

        assertThatCode(() -> ConveyorTasks.executeTasks(bindings))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("entries")
    void givenDirectory_whenExecuteTasks_thenDirectoryIsDeleted(
        Set<String> entries,
        @TempDir Path path
    ) throws IOException {
        Directories.writeFiles(path, entries);

        ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of("directory", path.toString())
            )
        );

        assertThat(path).doesNotExist();
    }
}