package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.exception.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDependencyDefinition;
import com.github.maximtereshchenko.conveyor.common.api.BuildFile;
import com.github.maximtereshchenko.conveyor.common.api.BuildFileType;
import com.github.maximtereshchenko.conveyor.common.api.BuildFiles;
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

        assertThatThrownBy(() -> module.build(nonExistent, Stage.COMPILE))
            .isInstanceOf(CouldNotFindProjectDefinition.class)
            .hasMessage(nonExistent.toString());
    }

    @Test
    void givenNoConveyorPluginsDeclared_whenBuild_thenNoBuildFiles(@TempDir Path path) {
        assertThat(module.build(conveyorJson(path), Stage.COMPILE))
            .isEqualTo(new BuildFiles());
    }

    @Test
    void givenConveyorPluginDeclared_whenBuild_thenTaskFromPluginExecuted(@TempDir Path path) throws Exception {
        assertThat(
            module.build(
                conveyorJson(path, new GeneratedConveyorPlugin("plugin").install(path)),
                Stage.COMPILE
            )
        )
            .isEqualTo(
                new BuildFiles(
                    new BuildFile(
                        defaultBuildDirectory(path).resolve("plugin-1-prepared"),
                        BuildFileType.ARTIFACT
                    ),
                    new BuildFile(
                        defaultBuildDirectory(path).resolve("plugin-1-run"),
                        BuildFileType.ARTIFACT
                    ),
                    new BuildFile(
                        defaultBuildDirectory(path).resolve("plugin-1-finalized"),
                        BuildFileType.ARTIFACT
                    )
                )
            );
    }

    @Test
    void givenTaskBindToCompileStage_whenBuildUntilCleanStage_thenTaskDidNotExecuted(@TempDir Path path)
        throws Exception {
        assertThat(
            module.build(
                conveyorJson(path, new GeneratedConveyorPlugin("plugin").install(path)),
                Stage.CLEAN
            )
        )
            .isEqualTo(new BuildFiles());
    }

    @Test
    void givenPluginsRequireCommonDependency_whenBuild_thenDependencyUsedWithHigherVersion(@TempDir Path path)
        throws Exception {
        var buildFiles = module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin(
                    "first-plugin",
                    new GeneratedDependency("dependency", 1).install(path)
                )
                    .install(path),
                new GeneratedConveyorPlugin(
                    "second-plugin",
                    new GeneratedDependency("dependency", 2).install(path)
                )
                    .install(path)
            ),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(
                    defaultBuildDirectory(path).resolve("first-plugin-1-run"),
                    BuildFileType.ARTIFACT
                ),
                new BuildFile(
                    defaultBuildDirectory(path).resolve("second-plugin-1-run"),
                    BuildFileType.ARTIFACT
                )
            );
        assertThat(defaultBuildDirectory(path))
            .isDirectoryContaining("glob:**dependency-2")
            .isDirectoryNotContaining("glob:**dependency-1");
    }

    @Test
    void givenPluginRequireTransitiveDependency_whenBuild_thenTransitiveDependencyLoaded(@TempDir Path path)
        throws Exception {
        var buildFiles = module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin(
                    "plugin",
                    new GeneratedDependency(
                        "dependency",
                        new GeneratedDependency("transitive").install(path)
                    )
                        .install(path)
                )
                    .install(path)
            ),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(
                    defaultBuildDirectory(path).resolve("plugin-1-run"),
                    BuildFileType.ARTIFACT
                )
            );
        assertThat(defaultBuildDirectory(path))
            .isDirectoryContaining("glob:**dependency-1")
            .isDirectoryContaining("glob:**transitive-1");
    }

    @Test
    void givenPluginRequireTransitiveDependency_whenBuildWithHigherVersion_thenTransitiveDependencyExcluded(
        @TempDir Path path
    ) throws Exception {
        var transitive = new GeneratedDependency("transitive", 1).install(path);
        var conveyorJson = conveyorJson(
            path,
            new GeneratedConveyorPlugin(
                "first-plugin",
                new GeneratedDependency(
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
                    "dependency",
                    new GeneratedDependency(
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

        var buildFiles = module.build(conveyorJson, Stage.COMPILE);

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(
                    defaultBuildDirectory(path).resolve("first-plugin-1-run"),
                    BuildFileType.ARTIFACT
                ),
                new BuildFile(
                    defaultBuildDirectory(path).resolve("second-plugin-1-run"),
                    BuildFileType.ARTIFACT
                )
            );
        assertThat(defaultBuildDirectory(path))
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
                    new GeneratedDependency("should-not-be-updated", 1)
                        .install(path),
                    new GeneratedDependency(
                        "can-affect-versions",
                        1,
                        new GeneratedDependency("should-not-be-updated", 2)
                            .install(path)
                    )
                        .install(path),
                    new GeneratedDependency(
                        "will-remove-dependency",
                        new GeneratedDependency("can-affect-versions", 2)
                            .install(path)
                    )
                        .install(path)
                )
                    .install(path)
            ),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path))
            .isDirectoryContaining("glob:**plugin-1-run")
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

        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
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

        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
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

        var defaultBuildDirectory = defaultBuildDirectory(path);
        var preparedTime = instant(defaultBuildDirectory.resolve("plugin-1-prepared"));
        var runTime = instant(defaultBuildDirectory.resolve("plugin-1-run"));
        var finalizedTime = instant(defaultBuildDirectory.resolve("plugin-1-finalized"));
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

        var defaultBuildDirectory = defaultBuildDirectory(path);
        var cleanPreparedTime = instant(defaultBuildDirectory.resolve("clean-1-prepared"));
        var cleanRunTime = instant(defaultBuildDirectory.resolve("clean-1-run"));
        var cleanFinalizedTime = instant(defaultBuildDirectory.resolve("clean-1-finalized"));
        var compilePreparedTime = instant(defaultBuildDirectory.resolve("compile-1-prepared"));
        var compileRunTime = instant(defaultBuildDirectory.resolve("compile-1-run"));
        var compileFinalizedTime = instant(defaultBuildDirectory.resolve("compile-1-finalized"));
        assertThat(cleanPreparedTime).isBefore(cleanRunTime);
        assertThat(cleanRunTime).isBefore(cleanFinalizedTime);
        assertThat(compilePreparedTime).isBefore(compileRunTime);
        assertThat(compileRunTime).isBefore(compileFinalizedTime);
        assertThat(compilePreparedTime).isAfter(cleanFinalizedTime);
    }

    @Test
    void givenDependenciesDeclared_whenBuild_thenPluginCanAccessModulePath(@TempDir Path path) throws Exception {
        var implementation = new GeneratedDependency("implementation").install(path);
        var test = new GeneratedDependency("test").install(path);

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

        var defaultBuildDirectory = defaultBuildDirectory(path);
        assertThat(modulePath(defaultBuildDirectory.resolve("plugin-1-module-path-implementation")))
            .containsExactly(implementation.jar());
        assertThat(modulePath(defaultBuildDirectory.resolve("plugin-1-module-path-test")))
            .containsExactly(test.jar());
    }

    @Test
    void givenProjectDirectoryProperty_whenBuild_thenProjectBuiltInSpecifiedDirectory(@TempDir Path path)
        throws Exception {
        var project = Files.createDirectory(path.resolve("project"));

        var buildFiles = module.build(
            conveyorJson(
                path,
                Map.of("conveyor.project.directory", project.toString()),
                new GeneratedConveyorPlugin("plugin").install(path)
            ),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(defaultBuildDirectory(project).resolve("plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenRelativeProjectDirectoryProperty_whenBuild_thenProjectDirectoryIsRelativeToWorkingDirectory(
        @TempDir Path path
    ) throws Exception {
        var project = Files.createDirectory(path.resolve("project"));

        var buildFiles = module.build(
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

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(defaultBuildDirectory(project).resolve("plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenProjectBuildDirectoryProperty_whenBuild_thenProjectBuiltInSpecifiedDirectory(
        @TempDir Path path
    ) throws Exception {
        var build = path.resolve("build");

        var buildFiles = module.build(
            conveyorJson(
                path,
                Map.of("conveyor.project.build.directory", build.toString()),
                new GeneratedConveyorPlugin("plugin").install(path)
            ),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(build.resolve("plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenRelativeProjectBuildDirectoryProperty_whenBuild_thenProjectBuildDirectoryIsRelativeToProjectDirectory(
        @TempDir Path path
    ) throws Exception {
        var project = path.resolve("project");

        var buildFiles = module.build(
            conveyorJson(
                path,
                Map.of(
                    "conveyor.project.directory", project.toString(),
                    "conveyor.project.build.directory", "./build"
                ),
                new GeneratedConveyorPlugin("plugin").install(path)
            ),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(project.resolve("build").resolve("plugin-1-run"), BuildFileType.ARTIFACT)
            );
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

    private Path defaultBuildDirectory(Path path) {
        return path.resolve(".conveyor");
    }
}
