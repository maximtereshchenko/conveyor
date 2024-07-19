package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.common.test.DirectoryEntriesSource;
import com.github.maximtereshchenko.conveyor.plugin.api.BindingStage;
import com.github.maximtereshchenko.conveyor.plugin.api.BindingStep;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.test.Dsl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class CleanPluginTests {

    @Test
    void givenPlugin_whenTasks_thenTaskBindToCleanRun(@TempDir Path path) throws IOException {
        new Dsl(new CleanPlugin(), path)
            .givenConfiguration("directory", path)
            .tasks()
            .contain(
                new ConveyorTask(
                    "clean",
                    BindingStage.CLEAN,
                    BindingStep.RUN,
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
        new Dsl(new CleanPlugin(), path)
            .givenConfiguration("directory", path.resolve("does-not-exist"))
            .tasks()
            .execute()
            .thenNoException();
    }

    @ParameterizedTest
    @DirectoryEntriesSource
    void givenDirectory_whenExecuteTasks_thenDirectoryIsDeleted(Path directory) throws IOException {
        new Dsl(new CleanPlugin(), directory)
            .givenConfiguration("directory", directory)
            .tasks()
            .execute();

        assertThat(directory).doesNotExist();
    }

    @Test
    void givenNoConfiguredDirectory_whenExecuteTasks_thenDefaultDirectoryIsDeleted(
        @TempDir Path path
    ) throws IOException {
        var defaultDirectory = Files.createDirectory(path.resolve(".conveyor"));

        new Dsl(new CleanPlugin(), path)
            .tasks()
            .execute();

        assertThat(defaultDirectory).doesNotExist();
    }
}