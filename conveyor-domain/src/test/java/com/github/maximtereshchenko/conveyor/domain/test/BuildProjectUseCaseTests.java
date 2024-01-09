package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.maximtereshchenko.conveyor.api.BuildSucceeded;
import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.domain.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.plugin.api.Stage;
import com.github.maximtereshchenko.conveyor.projectdefinitionreader.gson.GsonProjectDefinitionReader;
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
    void givenProjectDefinition_whenBuild_thenProjectIsBuilt(
        @TestProject("project-with-single-plugin") Path project
    ) {
        var conveyorJson = conveyorJson(project);

        assertThat(module.build(conveyorJson, Stage.COMPILE))
            .isEqualTo(new BuildSucceeded(conveyorJson, "project", 1));
    }

    @Test
    void givenConveyorPluginDefined_whenBuild_thenTaskFromPluginExecuted(
        @TestProject("project-with-single-plugin") Path project
    ) {
        module.build(conveyorJson(project), Stage.COMPILE);

        assertThat(project).isDirectoryContaining("glob:**file");
    }

    @Test
    void givenTaskBindToCompileStage_whenBuildUntilCleanStage_thenTaskDidNotExecuted(
        @TestProject("project-with-single-plugin") Path project
    ) {
        module.build(conveyorJson(project), Stage.CLEAN);

        assertThat(project).isDirectoryNotContaining("glob:**file");
    }

    @Test
    void givenPluginsRequireCommonDependency_whenBuild_thenDependencyUsedWithHigherVersion(
        @TestProject("project-with-plugins-sharing-common-dependency") Path project
    ) {
        module.build(conveyorJson(project), Stage.COMPILE);

        assertThat(project).isDirectoryContaining("glob:**first-2")
            .isDirectoryContaining("glob:**second-2");
    }

    @Test
    void givenPluginRequireTransitiveDependency_whenBuild_thenTransitiveDependencyLoaded(
        @TestProject("project-with-plugin-transitive-dependency") Path project
    ) {
        module.build(conveyorJson(project), Stage.COMPILE);

        assertThat(project).isDirectoryContaining("glob:**first-1-transitive");
    }

    private Path conveyorJson(Path path) {
        return path.resolve("conveyor.json");
    }
}
