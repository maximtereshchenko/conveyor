package com.github.maximtereshchenko.conveyor.plugin.clean;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.api.Project;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class CleanConveyorPluginTests {

    private final ConveyorPlugin plugin = new CleanConveyorPlugin();

    @Test
    void givenConveyorPlugin_whenBindings_thenOneBindingToCleanRunReturned(@TempDir Path path) {
        assertThat(plugin.bindings(project(path), Map.of()))
            .hasSize(1)
            .satisfiesExactly(binding ->
                assertThat(binding)
                    .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
                    .containsExactly(Stage.CLEAN, Step.RUN)
            );
    }

    @Test
    void givenNonExistentBuildDirectory_whenExecuteTask_thenNothingChanged(@TempDir Path path) {
        executeTask(path.resolve("build"));

        assertThat(path).isEmptyDirectory();
    }

    @Test
    void givenEmptyBuildDirectory_whenExecuteTask_thenDirectoryRemoved(@TempDir Path path) throws Exception {
        executeTask(Files.createDirectory(path.resolve("build")));

        assertThat(path).isEmptyDirectory();
    }

    @Test
    void givenBuildDirectoryContainsFile_whenExecuteTask_thenDirectoryRemoved(@TempDir Path path) throws Exception {
        var build = Files.createDirectory(path.resolve("build"));
        Files.createFile(build.resolve("file"));

        executeTask(build);

        assertThat(path).isEmptyDirectory();
    }

    @Test
    void givenBuildDirectoryContainsDirectory_whenExecuteTask_thenDirectoryRemoved(@TempDir Path path)
        throws Exception {
        var build = Files.createDirectory(path.resolve("build"));
        Files.createDirectory(build.resolve("directory"));

        executeTask(build);

        assertThat(path).isEmptyDirectory();
    }

    @Test
    void givenBuildDirectoryContainsNonEmptyDirectory_whenExecuteTask_thenDirectoryRemoved(@TempDir Path path)
        throws Exception {
        var build = Files.createDirectory(path.resolve("build"));
        Files.createFile(Files.createDirectory(build.resolve("directory")).resolve("file"));

        executeTask(build);

        assertThat(path).isEmptyDirectory();
    }

    Project project(Path buildDirectory) {
        return new Project() {

            @Override
            public Path projectDirectory() {
                return null;
            }

            @Override
            public Path buildDirectory() {
                return buildDirectory;
            }

            @Override
            public Set<Path> modulePath(DependencyScope... scopes) {
                return Set.of();
            }
        };
    }

    private void executeTask(Path path) {
        plugin.bindings(project(path), Map.of()).iterator().next().task().execute();
    }
}