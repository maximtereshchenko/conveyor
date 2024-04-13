package com.github.maximtereshchenko.conveyor.plugin.clean.test;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTaskBindings;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.test.common.Directories;
import com.github.maximtereshchenko.test.common.JimfsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(JimfsExtension.class)
final class CleanPluginTest {

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @Test
    void givenPlugin_whenBindings_thenTaskBindToCleanRun(Path path) {
        ConveyorTaskBindings.from(FakeConveyorSchematicBuilder.discoveryDirectory(path).build())
            .assertThat()
            .hasSize(1)
            .first()
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .containsExactly(Stage.CLEAN, Step.RUN);
    }

    @Test
    void givenNoDirectory_whenExecuteTasks_thenTaskDidNotFail(Path path) {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();
        var bindings = ConveyorTaskBindings.from(schematic);

        assertThatCode(bindings::executeTasks).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("entries")
    void givenDirectory_whenExecuteTasks_thenDirectoryIsDeleted(Set<String> entries, Path path)
        throws IOException {
        ConveyorTaskBindings.from(
                FakeConveyorSchematicBuilder.discoveryDirectory(path)
                    .constructionDirectory(Directories.writeFiles(path, entries))
                    .build()
            )
            .executeTasks();

        assertThat(path).doesNotExist();
    }
}