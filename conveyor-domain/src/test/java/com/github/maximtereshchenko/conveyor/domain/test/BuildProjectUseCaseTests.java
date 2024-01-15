package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.maximtereshchenko.conveyor.api.BuildSucceeded;
import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDependencyDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.domain.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class BuildProjectUseCaseTests {

    private final GsonAdapter gsonAdapter = new GsonAdapter();
    private final ConveyorModule module = new ConveyorFacade(gsonAdapter, gsonAdapter);

    @Test
    void givenNoProjectDefinition_whenBuild_thenCouldNotFindProjectDefinitionReturned(@TempDir Path path) {
        var nonExistent = path.resolve("non-existent.json");

        assertThat(module.build(nonExistent, Stage.COMPILE)).isEqualTo(new CouldNotFindProjectDefinition(nonExistent));
    }

    @Test
    void givenProjectDefinition_whenBuild_thenProjectIsBuilt(@TempDir Path path) {
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

        assertThat(path).isDirectoryContaining("glob:**plugin-1-run");
    }

    @Test
    void givenTaskBindToCompileStage_whenBuildUntilCleanStage_thenTaskDidNotExecuted(@TempDir Path path)
        throws Exception {
        module.build(
            conveyorJson(path, new GeneratedConveyorPlugin("plugin").install(path)),
            Stage.CLEAN
        );

        assertThat(path).isDirectoryNotContaining("glob:**plugin-1-run");
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

        assertThat(path).isDirectoryContaining("glob:**first-plugin-1-run")
            .isDirectoryContaining("glob:**second-plugin-1-run")
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

        assertThat(path).isDirectoryContaining("glob:**plugin-1-run")
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

        assertThat(path).isDirectoryContaining("glob:**first-plugin-1-run")
            .isDirectoryContaining("glob:**second-plugin-1-run")
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

        assertThat(path).isDirectoryContaining("glob:**plugin-1-run")
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
                Map.of(),
                List.of(new PluginDefinition("plugin", 1, Map.of("property", "value"))),
                List.of()
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
                Map.of("property", "value"),
                List.of(new PluginDefinition("plugin", 1, Map.of("property", "${property}-suffix"))),
                List.of()
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
                Map.of(),
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

    @Test
    void givenProjectDirectoryProperty_whenBuild_thenProjectBuiltInSpecifiedDirectory(@TempDir Path path)
        throws Exception {
        var project = Files.createDirectory(path.resolve("project"));

        module.build(
            conveyorJson(
                path,
                Map.of("conveyor.project.directory", project.toString()),
                new GeneratedConveyorPlugin("plugin").install(path)
            ),
            Stage.COMPILE
        );

        assertThat(project).isDirectoryContaining("glob:**plugin-1-run");
    }

    @Test
    void givenRelativeProjectDirectoryProperty_whenBuild_thenProjectDirectoryIsRelativeToWorkingDirectory(
        @TempDir Path path
    ) throws Exception {
        var project = Files.createDirectory(path.resolve("project"));

        module.build(
            conveyorJson(
                path,
                Map.of(
                    "conveyor.project.directory",
                    Paths.get("").toAbsolutePath().relativize(project).toString()
                ),
                new GeneratedConveyorPlugin("plugin").install(path)
            ),
            Stage.COMPILE
        );

        assertThat(project).isDirectoryContaining("glob:**plugin-1-run");
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

    private Path conveyorJson(Path path, GeneratedArtifactDefinition... plugins) {
        return conveyorJson(path, Map.of(), List.of(plugins), Map.of());
    }

    private Path conveyorJson(Path path, Map<String, String> properties, GeneratedArtifactDefinition... plugins) {
        return conveyorJson(path, properties, List.of(plugins), Map.of());
    }

    private Path conveyorJson(
        Path path,
        Map<String, String> properties,
        Collection<GeneratedArtifactDefinition> plugins,
        Map<GeneratedArtifactDefinition, DependencyScope> dependencies
    ) {
        return conveyorJson(
            path,
            properties,
            plugins.stream()
                .map(definition -> new PluginDefinition(definition.name(), definition.version(), Map.of()))
                .toList(),
            dependencies.entrySet()
                .stream()
                .map(entry ->
                    new ProjectDependencyDefinition(
                        entry.getKey().name(),
                        entry.getKey().version(),
                        entry.getValue()
                    )
                )
                .toList()
        );
    }

    private Path conveyorJson(
        Path path,
        Map<String, String> properties,
        Collection<PluginDefinition> plugins,
        Collection<ProjectDependencyDefinition> dependencies
    ) {
        return conveyorJson(
            path,
            new ProjectDefinition("project", 1, path, properties, plugins, dependencies)
        );
    }

    private Path conveyorJson(Path path, ProjectDefinition projectDefinition) {
        var conveyorJson = path.resolve("conveyor.json");
        gsonAdapter.write(conveyorJson, projectDefinition);
        return conveyorJson;
    }
}
