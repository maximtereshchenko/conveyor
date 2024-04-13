package com.github.maximtereshchenko.conveyor.plugin.clean.test;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTaskBindings;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.jimfs.JimfsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(JimfsExtension.class)
final class CleanPluginTest {

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

    @Test
    void givenEmptyDirectory_whenExecuteTasks_thenDirectoryIsDeleted(Path path) {
        ConveyorTaskBindings.from(
                FakeConveyorSchematicBuilder.discoveryDirectory(path)
                    .constructionDirectory(path)
                    .build()
            )
            .executeTasks();

        assertThat(path).doesNotExist();
    }

    @Test
    void givenDirectoryContainsFile_whenExecuteTasks_thenDirectoryIsDeleted(Path path)
        throws IOException {
        Files.createFile(path.resolve("file"));

        ConveyorTaskBindings.from(
                FakeConveyorSchematicBuilder.discoveryDirectory(path)
                    .constructionDirectory(path)
                    .build()
            )
            .executeTasks();

        assertThat(path).doesNotExist();
    }

    @Test
    void givenDirectoryContainsOtherDirectory_whenExecuteTasks_thenDirectoryIsDeleted(Path path)
        throws IOException {
        Files.createDirectory(path.resolve("directory"));

        ConveyorTaskBindings.from(
                FakeConveyorSchematicBuilder.discoveryDirectory(path)
                    .constructionDirectory(path)
                    .build()
            )
            .executeTasks();

        assertThat(path).doesNotExist();
    }

    @Test
    void givenDirectoryContainsNotEmptyDirectory_whenExecuteTasks_thenDirectoryIsDeleted(Path path)
        throws IOException {
        Files.createFile(
            Files.createDirectory(path.resolve("directory")).resolve("file")
        );

        ConveyorTaskBindings.from(
                FakeConveyorSchematicBuilder.discoveryDirectory(path)
                    .constructionDirectory(path)
                    .build()
            )
            .executeTasks();

        assertThat(path).doesNotExist();
    }
}