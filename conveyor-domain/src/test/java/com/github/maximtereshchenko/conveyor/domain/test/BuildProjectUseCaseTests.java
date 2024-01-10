package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.maximtereshchenko.conveyor.api.BuildSucceeded;
import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.domain.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.plugin.api.Stage;
import com.github.maximtereshchenko.conveyor.projectdefinitionreader.gson.GsonProjectDefinitionReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class BuildProjectUseCaseTests {

    private final ConveyorModule module = new ConveyorFacade(new GsonProjectDefinitionReader());

    @Test
    void givenNoProjectDefinition_whenBuild_thenCouldNotFindProjectDefinitionReturned(@TempDir Path path) {
        var nonExistent = path.resolve("non-existent.json");

        assertThat(module.build(nonExistent, Stage.COMPILE)).isEqualTo(new CouldNotFindProjectDefinition(nonExistent));
    }

    @Test
    void givenProjectDefinition_whenBuild_thenProjectIsBuilt(@TempDir Path path) throws Exception {
        var conveyorJson = Files.writeString(
            conveyorJson(path),
            """
                {
                   "name": "project",
                   "version": 1
                }
                """
        );

        assertThat(module.build(conveyorJson, Stage.COMPILE))
            .isEqualTo(new BuildSucceeded(conveyorJson, "project", 1));
    }

    @Test
    void givenConveyorPluginDefined_whenBuild_thenTaskFromPluginExecuted(@TempDir Path path) throws Exception {
        new GeneratedConveyorPlugin("plugin", 1).install(path);

        module.build(
            Files.writeString(
                conveyorJson(path),
                """
                    {
                       "version": 1,
                       "plugins": [
                         {
                           "name": "plugin",
                           "version": 1
                         }
                       ]
                     }
                    """
            ),
            Stage.COMPILE
        );

        assertThat(path).isDirectoryContaining("glob:**plugin-1-task-executed");
    }

    @Test
    void givenTaskBindToCompileStage_whenBuildUntilCleanStage_thenTaskDidNotExecuted(@TempDir Path path)
        throws Exception {
        new GeneratedConveyorPlugin("plugin", 1).install(path);

        module.build(
            Files.writeString(
                conveyorJson(path),
                """
                    {
                       "version": 1,
                       "plugins": [
                         {
                           "name": "plugin",
                           "version": 1
                         }
                       ]
                     }
                    """
            ),
            Stage.CLEAN
        );

        assertThat(path).isDirectoryNotContaining("glob:**plugin-1-task-executed");
    }

    @Test
    void givenPluginsRequireCommonDependency_whenBuild_thenDependencyUsedWithHigherVersion(@TempDir Path path)
        throws Exception {
        new GeneratedConveyorPlugin(
            "first-plugin",
            1,
            new GeneratedDependency(path, "dependency", 1).install(path)
        )
            .install(path);
        new GeneratedConveyorPlugin(
            "second-plugin",
            1,
            new GeneratedDependency(path, "dependency", 2).install(path)
        )
            .install(path);
        var conveyorJson = Files.writeString(
            conveyorJson(path),
            """
                {
                   "version": 1,
                   "plugins": [
                     {
                       "name": "first-plugin",
                       "version": 1
                     },
                     {
                       "name": "second-plugin",
                       "version": 1
                     }
                   ]
                 }
                """
        );

        module.build(conveyorJson, Stage.COMPILE);

        assertThat(path).isDirectoryContaining("glob:**first-plugin-1-task-executed")
            .isDirectoryContaining("glob:**second-plugin-1-task-executed")
            .isDirectoryContaining("glob:**dependency-2")
            .isDirectoryNotContaining("glob:**dependency-1");
    }

    @Test
    void givenPluginRequireTransitiveDependency_whenBuild_thenTransitiveDependencyLoaded(@TempDir Path path)
        throws Exception {
        new GeneratedConveyorPlugin(
            "plugin",
            1,
            new GeneratedDependency(
                path,
                "dependency",
                1,
                new GeneratedDependency(path, "transitive", 1).install(path)
            )
                .install(path)
        )
            .install(path);
        var conveyorJson = Files.writeString(
            conveyorJson(path),
            """
                {
                   "version": 1,
                   "plugins": [
                     {
                       "name": "plugin",
                       "version": 1
                     }
                   ]
                 }
                """
        );

        module.build(conveyorJson, Stage.COMPILE);

        assertThat(path).isDirectoryContaining("glob:**plugin-1-task-executed")
            .isDirectoryContaining("glob:**dependency-1")
            .isDirectoryContaining("glob:**transitive-1");
    }

    private Path conveyorJson(Path path) {
        return path.resolve("conveyor.json");
    }
}
