package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.exception.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.DependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.NoExplicitParent;
import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
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
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class BuildProjectUseCaseTests {

    private final GsonAdapter gsonAdapter = new GsonAdapter();
    private final ConveyorModule module = new ConveyorFacade(gsonAdapter);

    @Test
    void givenNoProjectDefinition_whenBuild_thenCouldNotFindProjectDefinitionReturned(@TempDir Path path) {
        var nonExistent = path.resolve("non-existent.json");

        assertThatThrownBy(() -> module.build(nonExistent, Stage.COMPILE))
            .isInstanceOf(CouldNotFindProjectDefinition.class)
            .hasMessage(nonExistent.toString());
    }

    @Test
    void givenNoConveyorPluginsDeclared_whenBuild_thenNoBuildFiles(@TempDir Path path) throws Exception {
        installSuperParent(path);

        assertThat(module.build(conveyorJson(path), Stage.COMPILE))
            .isEqualTo(new BuildFiles());
    }

    @Test
    void givenConveyorPluginDeclared_whenBuild_thenTaskFromPluginExecuted(@TempDir Path path) throws Exception {
        installSuperParent(path);

        assertThat(
            module.build(
                conveyorJson(path, new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path)),
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
        installSuperParent(path);

        assertThat(
            module.build(
                conveyorJson(path, new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path)),
                Stage.CLEAN
            )
        )
            .isEqualTo(new BuildFiles());
    }

    @Test
    void givenPluginsRequireCommonDependency_whenBuild_thenDependencyUsedWithHigherVersion(@TempDir Path path)
        throws Exception {
        installSuperParent(path);

        var buildFiles = module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin(
                    gsonAdapter,
                    "first-plugin",
                    new GeneratedDependency(gsonAdapter, "dependency", 1).install(path)
                )
                    .install(path),
                new GeneratedConveyorPlugin(
                    gsonAdapter,
                    "second-plugin",
                    new GeneratedDependency(gsonAdapter, "dependency", 2).install(path)
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
        installSuperParent(path);

        var buildFiles = module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin(
                    gsonAdapter,
                    "plugin",
                    new GeneratedDependency(
                        gsonAdapter,
                        "dependency",
                        new GeneratedDependency(gsonAdapter, "transitive").install(path)
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
        installSuperParent(path);
        var transitive = new GeneratedDependency(gsonAdapter, "transitive", 1).install(path);
        var conveyorJson = conveyorJson(
            path,
            new GeneratedConveyorPlugin(
                gsonAdapter,
                "first-plugin",
                new GeneratedDependency(
                    gsonAdapter,
                    "common-dependency",
                    1,
                    transitive
                )
                    .install(path)
            )
                .install(path),
            new GeneratedConveyorPlugin(
                gsonAdapter,
                "second-plugin",
                new GeneratedDependency(
                    gsonAdapter,
                    "dependency",
                    new GeneratedDependency(
                        gsonAdapter,
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
        installSuperParent(path);

        module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin(
                    gsonAdapter,
                    "plugin",
                    new GeneratedDependency(gsonAdapter, "should-not-be-updated", 1)
                        .install(path),
                    new GeneratedDependency(
                        gsonAdapter,
                        "can-affect-versions",
                        1,
                        new GeneratedDependency(gsonAdapter, "should-not-be-updated", 2)
                            .install(path)
                    )
                        .install(path),
                    new GeneratedDependency(
                        gsonAdapter,
                        "will-remove-dependency",
                        new GeneratedDependency(gsonAdapter, "can-affect-versions", 2)
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
        installSuperParent(path);
        new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path);

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
        installSuperParent(path);
        new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path);

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
        installSuperParent(path);
        module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path)
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
        installSuperParent(path);

        module.build(
            conveyorJson(
                path,
                new GeneratedConveyorPlugin(gsonAdapter, "clean", Stage.CLEAN).install(path),
                new GeneratedConveyorPlugin(gsonAdapter, "compile", Stage.COMPILE).install(path)
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
        installSuperParent(path);
        var implementation = new GeneratedDependency(gsonAdapter, "implementation").install(path);
        var test = new GeneratedDependency(gsonAdapter, "test").install(path);

        module.build(
            conveyorJson(
                path,
                Map.of(),
                List.of(new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path)),
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
        installSuperParent(path);
        var project = Files.createDirectory(path.resolve("project"));

        var buildFiles = module.build(
            conveyorJson(
                path,
                Map.of("conveyor.project.directory", project.toString()),
                new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path)
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
        installSuperParent(path);
        var project = Files.createDirectory(path.resolve("project"));

        var buildFiles = module.build(
            conveyorJson(
                path,
                Map.of(
                    "conveyor.project.directory",
                    Paths.get("").toAbsolutePath().relativize(project).toString()
                ),
                new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path)
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
        installSuperParent(path);
        var build = path.resolve("build");

        var buildFiles = module.build(
            conveyorJson(
                path,
                Map.of("conveyor.project.build.directory", build.toString()),
                new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path)
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
        installSuperParent(path);
        var project = path.resolve("project");

        var buildFiles = module.build(
            conveyorJson(
                path,
                Map.of(
                    "conveyor.project.directory", project.toString(),
                    "conveyor.project.build.directory", "./build"
                ),
                new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path)
            ),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(project.resolve("build").resolve("plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenNoParentDeclared_whenBuild_thenProjectInheritedPluginsFromSuperParent(@TempDir Path path)
        throws Exception {
        installSuperParent(path, new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path));

        var buildFiles = module.build(
            conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(defaultBuildDirectory(path).resolve("plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenPluginDeclaredInChildWithDifferentVersion_whenBuild_thenPluginVersionInChildShouldTakePrecedence(
        @TempDir Path path
    ) throws Exception {
        installSuperParent(path, new GeneratedConveyorPlugin(gsonAdapter, "plugin", 2).install(path));

        var buildFiles = module.build(
            conveyorJson(path, new GeneratedConveyorPlugin(gsonAdapter, "plugin", 1).install(path)),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(defaultBuildDirectory(path).resolve("plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenPluginDeclaredInChildWithDifferentConfigurationValue_whenBuild_thenValueInChildShouldTakePrecedence(
        @TempDir Path path
    ) throws Exception {
        installSuperParent(
            path,
            Map.of("key", "parent-value"),
            new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path)
        );

        module.build(
            conveyorJson(
                path,
                Map.of(),
                List.of(new PluginDefinition("plugin", 1, Map.of("key", "child-value"))),
                List.of()
            ),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("key=child-value");
    }

    @Test
    void givenPluginDeclaredInChildWithAdditionalConfiguration_whenBuild_thenPluginConfigurationShouldBeMerged(
        @TempDir Path path
    ) throws Exception {
        installSuperParent(
            path,
            Map.of("parent-key", "value"),
            new GeneratedConveyorPlugin(gsonAdapter, "plugin").install(path)
        );

        module.build(
            conveyorJson(
                path,
                Map.of(),
                List.of(new PluginDefinition("plugin", 1, Map.of("child-key", "value"))),
                List.of()
            ),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .containsIgnoringNewLines("parent-key=value", "child-key=value");
    }

    private void installSuperParent(Path path, GeneratedArtifactDefinition... plugins) {
        installSuperParent(path, Map.of(), plugins);
    }

    private void installSuperParent(
        Path path,
        Map<String, String> configuration,
        GeneratedArtifactDefinition... plugins
    ) {
        gsonAdapter.write(
            path.resolve("super-parent-1.json"),
            new ProjectDefinition(
                "super-parent",
                1,
                new NoExplicitParent(),
                null,
                Map.of(),
                Stream.of(plugins)
                    .map(definition -> new PluginDefinition(definition.name(), definition.version(), configuration))
                    .toList(),
                List.of()
            )
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
                    new DependencyDefinition(
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
        Collection<DependencyDefinition> dependencies
    ) {
        return conveyorJson(
            path,
            new ProjectDefinition("project", 1, null, path, properties, plugins, dependencies)
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
