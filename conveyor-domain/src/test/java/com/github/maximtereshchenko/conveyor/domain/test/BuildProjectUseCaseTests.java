package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.maximtereshchenko.conveyor.api.BuildSucceeded;
import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.domain.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.projectdefinitionreader.gson.GsonProjectDefinitionReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
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
    void givenConveyorPluginDeclared_whenBuild_thenTaskFromPluginExecuted(@TempDir Path path) throws Exception {
        module.build(
            conveyorJson(path, new GeneratedConveyorPlugin("plugin").install(path)),
            Stage.COMPILE
        );

        assertThat(path).isDirectoryContaining("glob:**plugin-1-configuration");
    }

    @Test
    void givenTaskBindToCompileStage_whenBuildUntilCleanStage_thenTaskDidNotExecuted(@TempDir Path path)
        throws Exception {
        module.build(
            conveyorJson(path, new GeneratedConveyorPlugin("plugin").install(path)),
            Stage.CLEAN
        );

        assertThat(path).isDirectoryNotContaining("glob:**plugin-1-configuration");
    }

    @Test
    void givenPluginsRequireCommonDependency_whenBuild_thenDependencyUsedWithHigherVersion(@TempDir Path path)
        throws Exception {
        module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin(
                    "first-plugin",
                    new GeneratedDependency(path, "dependency", 1).install(path)
                )
                    .install(path),
                new GeneratedConveyorPlugin(
                    "second-plugin",
                    new GeneratedDependency(path, "dependency", 2).install(path)
                )
                    .install(path)
            ),
            Stage.COMPILE
        );

        assertThat(path).isDirectoryContaining("glob:**first-plugin-1-configuration")
            .isDirectoryContaining("glob:**second-plugin-1-configuration")
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
                    new GeneratedDependency(
                        path,
                        "dependency",
                        new GeneratedDependency(path, "transitive").install(path)
                    )
                        .install(path)
                )
                    .install(path)
            ),
            Stage.COMPILE
        );

        assertThat(path).isDirectoryContaining("glob:**plugin-1-configuration")
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
                new GeneratedDependency(
                    path,
                    "dependency",
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

        assertThat(path).isDirectoryContaining("glob:**first-plugin-1-configuration")
            .isDirectoryContaining("glob:**second-plugin-1-configuration")
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
                        new GeneratedDependency(path, "can-affect-versions", 2).install(path)
                    )
                        .install(path)
                )
                    .install(path)
            ),
            Stage.COMPILE
        );

        assertThat(path).isDirectoryContaining("glob:**plugin-1-configuration")
            .isDirectoryContaining("glob:**will-remove-dependency-1")
            .isDirectoryContaining("glob:**can-affect-versions-2")
            .isDirectoryContaining("glob:**should-not-be-updated-1")
            .isDirectoryNotContaining("glob:**should-not-be-updated-2");
    }

    @Test
    void givenPluginConfiguration_whenBuild_thenPluginCanSeeItsConfiguration(@TempDir Path path) throws Exception {
        new GeneratedConveyorPlugin("plugin").install(path);
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

        assertThat(path.resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("property=value");
    }

    @Test
    void givenProperty_whenBuild_thenPropertyInterpolatedIntoPluginConfiguration(@TempDir Path path) throws Exception {
        new GeneratedConveyorPlugin("plugin").install(path);
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

        assertThat(path.resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("property=value-suffix");
    }

    @Test
    void givenPluginDeclared_whenBuild_thenTasksShouldRunInStepOrder(@TempDir Path path) throws Exception {
        module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin("plugin").install(path)
            ),
            Stage.COMPILE
        );

        var preparedTime = instant(path.resolve("plugin-1-prepared"));
        var runTime = instant(path.resolve("plugin-1-run"));
        var finalizedTime = instant(path.resolve("plugin-1-finalized"));
        assertThat(preparedTime).isBefore(runTime);
        assertThat(runTime).isBefore(finalizedTime);
    }

    @Test
    void givenMultiplePlugins_whenBuild_thenTasksShouldRunInStageOrder(@TempDir Path path) throws Exception {
        module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin("clean", Stage.CLEAN).install(path),
                new GeneratedConveyorPlugin("compile", Stage.COMPILE).install(path)
            ),
            Stage.COMPILE
        );

        var cleanPreparedTime = instant(path.resolve("clean-1-prepared"));
        var cleanRunTime = instant(path.resolve("clean-1-run"));
        var cleanFinalizedTime = instant(path.resolve("clean-1-finalized"));
        var compilePreparedTime = instant(path.resolve("compile-1-prepared"));
        var compileRunTime = instant(path.resolve("compile-1-run"));
        var compileFinalizedTime = instant(path.resolve("compile-1-finalized"));
        assertThat(cleanPreparedTime).isBefore(cleanRunTime);
        assertThat(cleanRunTime).isBefore(cleanFinalizedTime);
        assertThat(compilePreparedTime).isBefore(compileRunTime);
        assertThat(compileRunTime).isBefore(compileFinalizedTime);
        assertThat(compilePreparedTime).isAfter(cleanFinalizedTime);
    }

    @Test
    void givenDependenciesDeclared_whenBuild_thenPluginCanAccessModulePath(@TempDir Path path) throws Exception {
        var implementation = new GeneratedDependency(path, "implementation").install(path);
        var test = new GeneratedDependency(path, "test").install(path);

        module.build(
            conveyorJson(
                path,
                List.of(new GeneratedConveyorPlugin("plugin").install(path)),
                Map.of(
                    implementation, DependencyScope.IMPLEMENTATION,
                    test, DependencyScope.TEST
                )
            ),
            Stage.COMPILE
        );

        assertThat(modulePath(path.resolve("plugin-1-module-path-implementation")))
            .containsExactly(implementation.jar());
        assertThat(modulePath(path.resolve("plugin-1-module-path-test")))
            .containsExactly(test.jar());
    }

    private Collection<Path> modulePath(Path path) throws IOException {
        return Files.readAllLines(path)
            .stream()
            .map(Paths::get)
            .toList();
    }

    private Instant instant(Path path) throws IOException {
        return Instant.parse(Files.readString(path));
    }

    private Path conveyorJson(Path path, GeneratedArtifactDefinition... plugins) throws IOException {
        return conveyorJson(path, List.of(plugins), Map.of());
    }

    private Path conveyorJson(
        Path path,
        Collection<GeneratedArtifactDefinition> plugins,
        Map<GeneratedArtifactDefinition, DependencyScope> dependencies
    ) throws IOException {
        return conveyorJson(
            path,
            """
                {
                   "name": "project",
                   "version": 1,
                   "plugins": [
                     %s
                   ],
                   "dependencies": [
                     %s
                   ]
                }
                """
                .formatted(json(plugins), json(dependencies))
        );
    }

    private String json(Map<GeneratedArtifactDefinition, DependencyScope> dependencies) {
        return dependencies.entrySet()
            .stream()
            .map(entry ->
                """
                    {
                       "name": "%s",
                       "version": %d,
                       "scope": "%s"
                    }
                    """
                    .formatted(
                        entry.getKey().name(),
                        entry.getKey().version(),
                        entry.getValue().toString().toLowerCase(Locale.ROOT)
                    )
            )
            .collect(Collectors.joining(","));
    }

    private String json(Collection<GeneratedArtifactDefinition> definitions) {
        return definitions.stream()
            .map(definition ->
                """
                    {
                       "name": "%s",
                       "version": %d
                    }
                    """
                    .formatted(definition.name(), definition.version())
            )
            .collect(Collectors.joining(","));
    }

    private Path conveyorJson(Path path, String json) throws IOException {
        return Files.writeString(path.resolve("conveyor.json"), json);
    }
}
