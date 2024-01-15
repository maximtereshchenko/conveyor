package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.maximtereshchenko.conveyor.api.BuildSucceeded;
import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.domain.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.plugin.api.Stage;
import com.github.maximtereshchenko.conveyor.projectdefinitionreader.gson.GsonProjectDefinitionReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        var conveyorJson = conveyorJson(path);

        assertThat(module.build(conveyorJson(path), Stage.COMPILE))
            .isEqualTo(new BuildSucceeded(conveyorJson, "project", 1));
    }

    @Test
    void givenConveyorPluginDefined_whenBuild_thenTaskFromPluginExecuted(@TempDir Path path) throws Exception {
        module.build(
            conveyorJson(path, new GeneratedConveyorPlugin("plugin", 1).install(path)),
            Stage.COMPILE
        );

        assertThat(path).isDirectoryContaining("glob:**plugin-1-task-executed");
    }

    @Test
    void givenTaskBindToCompileStage_whenBuildUntilCleanStage_thenTaskDidNotExecuted(@TempDir Path path)
        throws Exception {
        module.build(
            conveyorJson(path, new GeneratedConveyorPlugin("plugin", 1).install(path)),
            Stage.CLEAN
        );

        assertThat(path).isDirectoryNotContaining("glob:**plugin-1-task-executed");
    }

    @Test
    void givenPluginsRequireCommonDependency_whenBuild_thenDependencyUsedWithHigherVersion(@TempDir Path path)
        throws Exception {
        module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin(
                    "first-plugin",
                    1,
                    new GeneratedDependency(path, "dependency", 1).install(path)
                )
                    .install(path),
                new GeneratedConveyorPlugin(
                    "second-plugin",
                    1,
                    new GeneratedDependency(path, "dependency", 2).install(path)
                )
                    .install(path)
            ),
            Stage.COMPILE
        );

        assertThat(path).isDirectoryContaining("glob:**first-plugin-1-task-executed")
            .isDirectoryContaining("glob:**second-plugin-1-task-executed")
            .isDirectoryContaining("glob:**dependency-2")
            .isDirectoryNotContaining("glob:**dependency-1");
    }

    @Test
    void givenPluginRequireTransitiveDependency_whenBuild_thenTransitiveDependencyLoaded(@TempDir Path path)
        throws Exception {
        module.build(
            conveyorJson(
                path,
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
                    .install(path)
            ),
            Stage.COMPILE
        );

        assertThat(path).isDirectoryContaining("glob:**plugin-1-task-executed")
            .isDirectoryContaining("glob:**dependency-1")
            .isDirectoryContaining("glob:**transitive-1");
    }

    @Test
    void givenPluginRequireTransitiveDependency_whenBuildWithHigherVersion_thenTransitiveDependencyExcluded(
        @TempDir Path path
    ) throws Exception {
        var transitive = new GeneratedDependency(path, "transitive", 1).install(path);
        var conveyorJson = conveyorJson(
            path,
            new GeneratedConveyorPlugin(
                "first-plugin",
                1,
                new GeneratedDependency(
                    path,
                    "common-dependency",
                    1,
                    transitive
                )
                    .install(path)
            )
                .install(path),
            new GeneratedConveyorPlugin(
                "second-plugin",
                1,
                new GeneratedDependency(
                    path,
                    "dependency",
                    1,
                    new GeneratedDependency(
                        path,
                        "common-dependency",
                        2
                    )
                        .install(path)
                )
                    .install(path)
            )
                .install(path)
        );
        Files.delete(transitive.jar());

        module.build(conveyorJson, Stage.COMPILE);

        assertThat(path).isDirectoryContaining("glob:**first-plugin-1-task-executed")
            .isDirectoryContaining("glob:**second-plugin-1-task-executed")
            .isDirectoryContaining("glob:**dependency-1")
            .isDirectoryContaining("glob:**common-dependency-2");
    }

    @Test
    void givenDependencyAffectResolvedVersions_whenBuildWithDependencyExcluded_thenItShouldNotAffectVersions(
        @TempDir Path path
    ) throws Exception {
        module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin(
                    "plugin",
                    1,
                    new GeneratedDependency(path, "should-not-be-updated", 1).install(path),
                    new GeneratedDependency(
                        path,
                        "can-affect-versions",
                        1,
                        new GeneratedDependency(path, "should-not-be-updated", 2).install(path)
                    )
                        .install(path),
                    new GeneratedDependency(
                        path,
                        "will-remove-dependency",
                        1,
                        new GeneratedDependency(path, "can-affect-versions", 2).install(path)
                    )
                        .install(path)
                )
                    .install(path)
            ),
            Stage.COMPILE
        );

        assertThat(path).isDirectoryContaining("glob:**plugin-1-task-executed")
            .isDirectoryContaining("glob:**will-remove-dependency-1")
            .isDirectoryContaining("glob:**can-affect-versions-2")
            .isDirectoryContaining("glob:**should-not-be-updated-1")
            .isDirectoryNotContaining("glob:**should-not-be-updated-2");
    }

    @Test
    void givenPluginConfiguration_whenBuild_thenPluginCanSeeItsConfiguration(
        @TempDir Path path
    ) throws Exception {
        new GeneratedConveyorPlugin("plugin", 1).install(path);
        module.build(
            conveyorJson(
                path,
                """
                    {
                       "name": "project",
                       "version": 1,
                       "plugins": [
                           {
                               "name": "plugin",
                               "version": 1,
                               "configuration": {
                                   "property": "value"
                               }
                           }
                       ]
                    }
                    """
            ),
            Stage.COMPILE
        );

        assertThat(path.resolve("plugin-1-task-executed"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("property=value");
    }

    @Test
    void givenProperty_whenBuild_thenPropertyInterpolatedIntoPluginConfiguration(
        @TempDir Path path
    ) throws Exception {
        new GeneratedConveyorPlugin("plugin", 1).install(path);
        module.build(
            conveyorJson(
                path,
                """
                    {
                       "name": "project",
                       "version": 1,
                       "properties": {
                           "property": "value"
                       },
                       "plugins": [
                           {
                               "name": "plugin",
                               "version": 1,
                               "configuration": {
                                   "property": "${property}-suffix"
                               }
                           }
                       ]
                    }
                    """
            ),
            Stage.COMPILE
        );

        assertThat(path.resolve("plugin-1-task-executed"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("property=value-suffix");
    }

    private Path conveyorJson(Path path, GeneratedArtifactDefinition... definitions) throws IOException {
        return conveyorJson(
            path,
            """
                {
                   "name": "project",
                   "version": 1,
                   "plugins": [
                     %s
                   ]
                }
                """
                .formatted(
                    Stream.of(definitions)
                        .map(definition ->
                            """
                                {
                                   "name": "%s",
                                   "version": %d
                                }
                                """
                                .formatted(definition.name(), definition.version())
                        )
                        .collect(Collectors.joining(","))
                )
        );
    }

    private Path conveyorJson(Path path, String json) throws IOException {
        return Files.writeString(path.resolve("conveyor.json"), json);
    }
}
