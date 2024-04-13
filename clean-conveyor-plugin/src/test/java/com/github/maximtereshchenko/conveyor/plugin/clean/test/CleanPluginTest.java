package com.github.maximtereshchenko.conveyor.plugin.clean.test;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.jimfs.JimfsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(JimfsExtension.class)
final class CleanPluginTest {

    @Test
    void givenPlugin_whenBindings_thenTaskBindToCleanRun(Path path) {
        assertThat(bindings(path))
            .hasSize(1)
            .first()
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .containsExactly(Stage.CLEAN, Step.RUN);
    }

    @Test
    void givenNoDirectory_whenExecuteTasks_thenTaskDidNotFail(Path path) {
        assertThatCode(() -> executeTask(path)).doesNotThrowAnyException();
    }

    @Test
    void givenEmptyDirectory_whenExecuteTasks_thenDirectoryIsDeleted(Path path) {
        executeTask(path);

        assertThat(path).doesNotExist();
    }

    @Test
    void givenDirectoryContainsFile_whenExecuteTasks_thenDirectoryIsDeleted(Path path)
        throws IOException {
        Files.createFile(path.resolve("file"));

        executeTask(path);

        assertThat(path).doesNotExist();
    }

    @Test
    void givenDirectoryContainsOtherDirectory_whenExecuteTasks_thenDirectoryIsDeleted(Path path)
        throws IOException {
        Files.createDirectory(path.resolve("directory"));

        executeTask(path);

        assertThat(path).doesNotExist();
    }

    @Test
    void givenDirectoryContainsNotEmptyDirectory_whenExecuteTasks_thenDirectoryIsDeleted(Path path)
        throws IOException {
        Files.createFile(
            Files.createDirectory(path.resolve("directory")).resolve("file")
        );

        executeTask(path);

        assertThat(path).doesNotExist();
    }

    private void executeTask(Path constructionDirectory) {
        bindings(constructionDirectory)
            .stream()
            .map(ConveyorTaskBinding::task)
            .forEach(task -> task.execute(Set.of()));
    }

    private List<ConveyorTaskBinding> bindings(Path constructionDirectory) {
        var schematic = new FakeConveyorSchematic(constructionDirectory);
        return ServiceLoader.load(ConveyorPlugin.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .map(conveyorPlugin -> conveyorPlugin.bindings(schematic, Map.of()))
            .flatMap(Collection::stream)
            .toList();
    }
}