package com.github.maximtereshchenko.conveyor.plugin.clean.test;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.clean.CleanPlugin;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
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
    void givenPlugin_whenBindings_thenTaskBindToCleanRun(@TempDir Path path) {
        assertThat(
            plugin.bindings(
                FakeConveyorSchematicBuilder.discoveryDirectory(path).build(),
                Map.of()
            )
        )
            .hasSize(1)
            .first()
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .containsExactly(Stage.CLEAN, Step.RUN);
    }

    @Test
    void givenNoDirectory_whenExecuteTasks_thenTaskDidNotFail(@TempDir Path path) {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        assertThatCode(() -> ConveyorTasks.executeTasks(plugin.bindings(schematic, Map.of())))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("entries")
    void givenDirectory_whenExecuteTasks_thenDirectoryIsDeleted(
        Set<String> entries,
        @TempDir Path path
    )
        throws IOException {
        ConveyorTasks.executeTasks(
            plugin.bindings(
                FakeConveyorSchematicBuilder.discoveryDirectory(path)
                    .constructionDirectory(Directories.writeFiles(path, entries))
                    .build(),
                Map.of()
            )
        );

        assertThat(path).doesNotExist();
    }
}