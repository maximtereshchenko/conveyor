package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.exception.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.common.api.BuildFile;
import com.github.maximtereshchenko.conveyor.common.api.BuildFileType;
import com.github.maximtereshchenko.conveyor.common.api.BuildFiles;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.domain.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
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
    void givenNoConveyorPluginsDeclared_whenBuild_thenNoBuildFiles(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        assertThat(module.build(ProjectDefinitionBuilder.conveyorJson(gsonAdapter).install(path), Stage.COMPILE))
            .isEqualTo(new BuildFiles());
    }

    @Test
    void givenConveyorPluginDeclared_whenBuild_thenTaskFromPluginExecuted(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        assertThat(
            module.build(
                ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                    .plugin(ConveyorPluginBuilder.empty(gsonAdapter))
                    .install(path),
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
    void givenTaskBindToCompileStage_whenBuildUntilCleanStage_thenTaskDidNotExecuted(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        assertThat(
            module.build(
                ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                    .plugin(ConveyorPluginBuilder.empty(gsonAdapter))
                    .install(path),
                Stage.CLEAN
            )
        )
            .isEqualTo(new BuildFiles());
    }

    @Test
    void givenPluginsRequireCommonDependency_whenBuild_thenDependencyUsedWithHigherVersion(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        var buildFiles = module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter)
                        .name("first-plugin")
                        .dependency(DependencyBuilder.empty(gsonAdapter))
                )
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter)
                        .name("second-plugin")
                        .dependency(
                            DependencyBuilder.empty(gsonAdapter)
                                .version(2)
                        )
                )
                .install(path),
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
    void givenPluginRequireTransitiveDependency_whenBuild_thenTransitiveDependencyLoaded(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        var buildFiles = module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter)
                        .dependency(
                            DependencyBuilder.empty(gsonAdapter)
                                .dependency(
                                    DependencyBuilder.empty(gsonAdapter)
                                        .name("transitive")
                                )
                        )
                )
                .install(path),
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
    void givenPluginRequireDependencyWithTestScope_whenBuild_thenDependencyIsNotLoaded(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        var conveyorJson = ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
            .plugin(
                ConveyorPluginBuilder.empty(gsonAdapter)
                    .dependency(ProjectDefinitionBuilder.empty(gsonAdapter, "test"), DependencyScope.TEST)
            )
            .install(path);

        assertThatCode(() -> module.build(conveyorJson, Stage.COMPILE)).doesNotThrowAnyException();
    }

    @Test
    void givenPluginRequireTransitiveDependency_whenBuildWithHigherVersion_thenTransitiveDependencyExcluded(
        @TempDir Path path
    ) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);
        var transitive = ProjectDefinitionBuilder.empty(gsonAdapter, "transitive");
        var commonDependency = DependencyBuilder.empty(gsonAdapter)
            .name("common-dependency");

        var buildFiles = module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter)
                        .name("first-plugin")
                        .dependency(
                            commonDependency.version(1)
                                .dependency(transitive)
                        )
                )
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter)
                        .name("second-plugin")
                        .dependency(
                            DependencyBuilder.empty(gsonAdapter)
                                .name("dependency")
                                .dependency(commonDependency.version(2))
                        )
                )
                .install(path),
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
            .isDirectoryContaining("glob:**dependency-1")
            .isDirectoryContaining("glob:**common-dependency-2");
    }

    @Test
    void givenDependencyAffectResolvedVersions_whenBuildWithDependencyExcluded_thenItShouldNotAffectVersions(
        @TempDir Path path
    ) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);
        var shouldNotBeUpdated = DependencyBuilder.empty(gsonAdapter)
            .name("should-not-be-updated");
        var canAffectVersions = DependencyBuilder.empty(gsonAdapter)
            .name("can-affect-versions");

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter)
                        .dependency(shouldNotBeUpdated.version(1))
                        .dependency(
                            canAffectVersions.version(1)
                                .dependency(shouldNotBeUpdated.version(2))
                        )
                        .dependency(
                            DependencyBuilder.empty(gsonAdapter)
                                .name("will-remove-dependency")
                                .dependency(canAffectVersions.version(2))
                        )
                )
                .install(path),
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
    void givenPluginConfiguration_whenBuild_thenPluginCanSeeItsConfiguration(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter),
                    Map.of("property", "value")
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("property=value");
    }

    @Test
    void givenProperty_whenBuild_thenPropertyInterpolatedIntoPluginConfiguration(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .property("property", "value")
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter),
                    Map.of("property", "${property}-suffix")
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("property=value-suffix");
    }

    @Test
    void givenPluginDeclared_whenBuild_thenTasksShouldRunInStepOrder(@TempDir Path path) throws Exception {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(ConveyorPluginBuilder.empty(gsonAdapter))
                .install(path),
            Stage.COMPILE
        );

        var preparedTime = instant(defaultBuildDirectory(path).resolve("plugin-1-prepared"));
        var runTime = instant(defaultBuildDirectory(path).resolve("plugin-1-run"));
        var finalizedTime = instant(defaultBuildDirectory(path).resolve("plugin-1-finalized"));
        assertThat(preparedTime).isBefore(runTime);
        assertThat(runTime).isBefore(finalizedTime);
    }

    @Test
    void givenMultiplePlugins_whenBuild_thenTasksShouldRunInStageOrder(@TempDir Path path) throws Exception {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter)
                        .name("clean")
                        .stage(Stage.CLEAN)
                )
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter)
                        .name("compile")
                        .stage(Stage.COMPILE)
                )
                .install(path),
            Stage.COMPILE
        );

        var cleanPreparedTime = instant(defaultBuildDirectory(path).resolve("clean-1-prepared"));
        var cleanRunTime = instant(defaultBuildDirectory(path).resolve("clean-1-run"));
        var cleanFinalizedTime = instant(defaultBuildDirectory(path).resolve("clean-1-finalized"));
        var compilePreparedTime = instant(defaultBuildDirectory(path).resolve("compile-1-prepared"));
        var compileRunTime = instant(defaultBuildDirectory(path).resolve("compile-1-run"));
        var compileFinalizedTime = instant(defaultBuildDirectory(path).resolve("compile-1-finalized"));
        assertThat(cleanPreparedTime).isBefore(cleanRunTime);
        assertThat(cleanRunTime).isBefore(cleanFinalizedTime);
        assertThat(compilePreparedTime).isBefore(compileRunTime);
        assertThat(compileRunTime).isBefore(compileFinalizedTime);
        assertThat(compilePreparedTime).isAfter(cleanFinalizedTime);
    }

    @Test
    void givenDependenciesDeclared_whenBuild_thenPluginCanAccessModulePath(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(ConveyorPluginBuilder.empty(gsonAdapter))
                .dependency(
                    DependencyBuilder.empty(gsonAdapter)
                        .name("implementation"),
                    DependencyScope.IMPLEMENTATION
                )
                .dependency(
                    DependencyBuilder.empty(gsonAdapter)
                        .name("test"),
                    DependencyScope.TEST
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(modulePath(defaultBuildDirectory(path).resolve("plugin-1-module-path-implementation")))
            .containsExactly(path.resolve("implementation-1.jar"));
        assertThat(modulePath(defaultBuildDirectory(path).resolve("plugin-1-module-path-test")))
            .containsExactly(path.resolve("test-1.jar"));
    }

    @Test
    void givenTransitiveDependencyWithTestScope_whenBuild_thenDependencyIsNotLoaded(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(ConveyorPluginBuilder.empty(gsonAdapter))
                .dependency(
                    DependencyBuilder.empty(gsonAdapter)
                        .dependency(
                            ProjectDefinitionBuilder.empty(gsonAdapter, "test"),
                            DependencyScope.TEST
                        )
                )
                .install(path),
            Stage.COMPILE
        );

        var testJar = path.resolve("test-1.jar");
        assertThat(modulePath(defaultBuildDirectory(path).resolve("plugin-1-module-path-implementation")))
            .doesNotContain(testJar);
        assertThat(modulePath(defaultBuildDirectory(path).resolve("plugin-1-module-path-test")))
            .doesNotContain(testJar);
    }

    @Test
    void givenProjectDirectoryProperty_whenBuild_thenProjectBuiltInSpecifiedDirectory(@TempDir Path path)
        throws Exception {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);
        var project = Files.createDirectory(path.resolve("project"));

        var buildFiles = module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .property("conveyor.project.directory", project.toString())
                .plugin(ConveyorPluginBuilder.empty(gsonAdapter))
                .install(path),
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
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);
        var project = Files.createDirectory(path.resolve("project"));

        var buildFiles = module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .property(
                    "conveyor.project.directory",
                    Paths.get("").toAbsolutePath().relativize(project).toString()
                )
                .plugin(ConveyorPluginBuilder.empty(gsonAdapter))
                .install(path),
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
    ) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);
        var build = path.resolve("build");

        var buildFiles = module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .property("conveyor.project.build.directory", build.toString())
                .plugin(ConveyorPluginBuilder.empty(gsonAdapter))
                .install(path),
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
    ) {
        ProjectDefinitionBuilder.superParent(gsonAdapter).install(path);
        var project = path.resolve("project");

        var buildFiles = module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .property("conveyor.project.directory", project.toString())
                .property("conveyor.project.build.directory", "./build")
                .plugin(ConveyorPluginBuilder.empty(gsonAdapter))
                .install(path),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(project.resolve("build").resolve("plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenNoParentDeclared_whenBuild_thenProjectInheritedPluginsFromSuperParent(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter)
            .plugin(ConveyorPluginBuilder.empty(gsonAdapter))
            .install(path);

        var buildFiles = module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter).install(path),
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
    ) {
        var plugin = ConveyorPluginBuilder.empty(gsonAdapter);
        ProjectDefinitionBuilder.superParent(gsonAdapter)
            .plugin(plugin.version(2))
            .install(path);

        var buildFiles = module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(plugin.version(1))
                .install(path),
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
    ) {
        var plugin = ConveyorPluginBuilder.empty(gsonAdapter);
        ProjectDefinitionBuilder.superParent(gsonAdapter)
            .plugin(plugin, Map.of("key", "parent-value"))
            .install(path);

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(plugin, Map.of("key", "child-value"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("key=child-value");
    }

    @Test
    void givenPluginDeclaredInChildWithAdditionalConfiguration_whenBuild_thenPluginConfigurationsAreMerged(
        @TempDir Path path
    ) {
        var plugin = ConveyorPluginBuilder.empty(gsonAdapter);
        ProjectDefinitionBuilder.superParent(gsonAdapter)
            .plugin(plugin, Map.of("parent-key", "value"))
            .install(path);

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(plugin, Map.of("child-key", "value"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .containsIgnoringNewLines("parent-key=value", "child-key=value");
    }

    @Test
    void givenParentHasDependencies_whenBuild_thenChildInheritedDependencies(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter)
            .dependency(
                DependencyBuilder.empty(gsonAdapter)
                    .name("implementation"),
                DependencyScope.IMPLEMENTATION
            )
            .dependency(
                DependencyBuilder.empty(gsonAdapter)
                    .name("test"),
                DependencyScope.TEST
            )
            .install(path);

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .plugin(ConveyorPluginBuilder.empty(gsonAdapter))
                .install(path),
            Stage.COMPILE
        );

        assertThat(modulePath(defaultBuildDirectory(path).resolve("plugin-1-module-path-implementation")))
            .containsExactly(path.resolve("implementation-1.jar"));
        assertThat(modulePath(defaultBuildDirectory(path).resolve("plugin-1-module-path-test")))
            .containsExactly(path.resolve("test-1.jar"));
    }

    @Test
    void givenChildHasDifferentProperty_whenBuild_thenPropertiesAreMerged(@TempDir Path path) throws Exception {
        ProjectDefinitionBuilder.superParent(gsonAdapter)
            .property("parent-key", "parent-value")
            .install(path);

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .property("child-key", "child-value")
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter),
                    Map.of(
                        "parent-key", "${parent-key}",
                        "child-key", "${child-key}"
                    )
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .containsIgnoringNewLines("parent-key=parent-value", "child-key=child-value");
    }

    @Test
    void givenChildHasPropertyWithSameKey_whenBuild_thenChildPropertyValueTookPrecedence(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter)
            .property("key", "parent-value")
            .install(path);

        module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .property("key", "child-value")
                .plugin(
                    ConveyorPluginBuilder.empty(gsonAdapter),
                    Map.of("key", "${key}")
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("key=child-value");
    }

    @Test
    void givenChildHasMultipleParents_whenBuild_thenChildInheritsFromAllParents(@TempDir Path path) {
        ProjectDefinitionBuilder.superParent(gsonAdapter)
            .property("key", "value")
            .install(path);

        var buildFiles = module.build(
            ProjectDefinitionBuilder.conveyorJson(gsonAdapter)
                .parent(
                    ProjectDefinitionBuilder.empty(gsonAdapter, "parent")
                        .parent(
                            ProjectDefinitionBuilder.empty(gsonAdapter, "grand-parent")
                                .plugin(
                                    ConveyorPluginBuilder.empty(gsonAdapter),
                                    Map.of("key", "${key}")
                                )
                        )
                        .dependency(DependencyBuilder.empty(gsonAdapter))
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(defaultBuildDirectory(path).resolve("plugin-1-run"), BuildFileType.ARTIFACT)
            );
        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("key=value");
        assertThat(modulePath(defaultBuildDirectory(path).resolve("plugin-1-module-path-implementation")))
            .containsExactly(path.resolve("dependency-1.jar"));
    }

    private Collection<Path> modulePath(Path path) {
        try {
            return Files.readAllLines(path)
                .stream()
                .map(Paths::get)
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Instant instant(Path path) {
        try {
            return Instant.parse(Files.readString(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path defaultBuildDirectory(Path path) {
        return path.resolve(".conveyor");
    }
}
