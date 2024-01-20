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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

@ExtendWith(ConveyorExtension.class)
final class BuildProjectUseCaseTests {

    @Test
    void givenNoProjectDefinition_whenBuild_thenCouldNotFindProjectDefinitionReturned(
        @TempDir Path path,
        ConveyorModule module
    ) {
        var nonExistent = path.resolve("non-existent.json");

        assertThatThrownBy(() -> module.build(nonExistent, Stage.COMPILE))
            .isInstanceOf(CouldNotFindProjectDefinition.class)
            .hasMessage(nonExistent.toString());
    }

    @Test
    void givenNoConveyorPluginsDeclared_whenBuild_thenNoBuildFiles(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        assertThat(module.build(factory.conveyorJson().install(path), Stage.COMPILE))
            .isEqualTo(new BuildFiles());
    }

    @Test
    void givenConveyorPluginDeclared_whenBuild_thenTaskFromPluginExecuted(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        assertThat(
            module.build(
                factory.conveyorJson()
                    .plugin(factory.plugin())
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
    void givenTaskBindToCompileStage_whenBuildUntilCleanStage_thenTaskDidNotExecuted(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        assertThat(
            module.build(
                factory.conveyorJson()
                    .plugin(factory.plugin())
                    .install(path),
                Stage.CLEAN
            )
        )
            .isEqualTo(new BuildFiles());
    }

    @Test
    void givenPluginsRequireCommonDependency_whenBuild_thenDependencyUsedWithHigherVersion(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        var buildFiles = module.build(
            factory.conveyorJson()
                .plugin(
                    factory.plugin()
                        .name("first-plugin")
                        .dependency(factory.dependency())
                )
                .plugin(
                    factory.plugin()
                        .name("second-plugin")
                        .dependency(
                            factory.dependency()
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
    void givenPluginRequireTransitiveDependency_whenBuild_thenTransitiveDependencyLoaded(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        var buildFiles = module.build(
            factory.conveyorJson()
                .plugin(
                    factory.plugin()
                        .dependency(
                            factory.dependency()
                                .dependency(
                                    factory.dependency()
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
    void givenPluginRequireDependencyWithTestScope_whenBuild_thenDependencyIsNotLoaded(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        var conveyorJson = factory.conveyorJson()
            .plugin(
                factory.plugin()
                    .dependency(factory.project("test"), DependencyScope.TEST)
            )
            .install(path);

        assertThatCode(() -> module.build(conveyorJson, Stage.COMPILE)).doesNotThrowAnyException();
    }

    @Test
    void givenPluginRequireTransitiveDependency_whenBuildWithHigherVersion_thenTransitiveDependencyExcluded(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);
        var transitive = factory.project("transitive");
        var commonDependency = factory.dependency()
            .name("common-dependency");

        var buildFiles = module.build(
            factory.conveyorJson()
                .plugin(
                    factory.plugin()
                        .name("first-plugin")
                        .dependency(
                            commonDependency.version(1)
                                .dependency(transitive)
                        )
                )
                .plugin(
                    factory.plugin()
                        .name("second-plugin")
                        .dependency(
                            factory.dependency()
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
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);
        var shouldNotBeUpdated = factory.dependency()
            .name("should-not-be-updated");
        var canAffectVersions = factory.dependency()
            .name("can-affect-versions");

        module.build(
            factory.conveyorJson()
                .plugin(
                    factory.plugin()
                        .dependency(shouldNotBeUpdated.version(1))
                        .dependency(
                            canAffectVersions.version(1)
                                .dependency(shouldNotBeUpdated.version(2))
                        )
                        .dependency(
                            factory.dependency()
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
    void givenPluginConfiguration_whenBuild_thenPluginCanSeeItsConfiguration(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        module.build(
            factory.conveyorJson()
                .plugin(
                    factory.plugin(),
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
    void givenProperty_whenBuild_thenPropertyInterpolatedIntoPluginConfiguration(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        module.build(
            factory.conveyorJson()
                .property("property", "value")
                .plugin(
                    factory.plugin(),
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
    void givenPluginDeclared_whenBuild_thenTasksShouldRunInStepOrder(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        module.build(
            factory.conveyorJson()
                .plugin(factory.plugin())
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
    void givenMultiplePlugins_whenBuild_thenTasksShouldRunInStageOrder(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        module.build(
            factory.conveyorJson()
                .plugin(
                    factory.plugin()
                        .name("clean")
                        .stage(Stage.CLEAN)
                )
                .plugin(
                    factory.plugin()
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
    void givenDependenciesDeclared_whenBuild_thenPluginCanAccessModulePath(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        module.build(
            factory.conveyorJson()
                .plugin(factory.plugin())
                .dependency(
                    factory.dependency()
                        .name("implementation"),
                    DependencyScope.IMPLEMENTATION
                )
                .dependency(
                    factory.dependency()
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
    void givenTransitiveDependencyWithTestScope_whenBuild_thenDependencyIsNotLoaded(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        module.build(
            factory.conveyorJson()
                .plugin(factory.plugin())
                .dependency(
                    factory.dependency()
                        .dependency(
                            factory.project("test"),
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
    void givenProjectDirectoryProperty_whenBuild_thenProjectBuiltInSpecifiedDirectory(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    )
        throws Exception {
        factory.superParent().install(path);
        var project = Files.createDirectory(path.resolve("project"));

        var buildFiles = module.build(
            factory.conveyorJson()
                .property("conveyor.project.directory", project.toString())
                .plugin(factory.plugin())
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
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) throws Exception {
        factory.superParent().install(path);
        var project = Files.createDirectory(path.resolve("project"));

        var buildFiles = module.build(
            factory.conveyorJson()
                .property(
                    "conveyor.project.directory",
                    Paths.get("").toAbsolutePath().relativize(project).toString()
                )
                .plugin(factory.plugin())
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
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);
        var build = path.resolve("build");

        var buildFiles = module.build(
            factory.conveyorJson()
                .property("conveyor.project.build.directory", build.toString())
                .plugin(factory.plugin())
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
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);
        var project = path.resolve("project");

        var buildFiles = module.build(
            factory.conveyorJson()
                .property("conveyor.project.directory", project.toString())
                .property("conveyor.project.build.directory", "./build")
                .plugin(factory.plugin())
                .install(path),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(project.resolve("build").resolve("plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenNoParentDeclared_whenBuild_thenProjectInheritedPluginsFromSuperParent(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent()
            .plugin(factory.plugin())
            .install(path);

        var buildFiles = module.build(
            factory.conveyorJson().install(path),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(defaultBuildDirectory(path).resolve("plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenPluginDeclaredInChildWithDifferentVersion_whenBuild_thenPluginVersionInChildShouldTakePrecedence(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        var plugin = factory.plugin();
        factory.superParent()
            .plugin(plugin.version(2))
            .install(path);

        var buildFiles = module.build(
            factory.conveyorJson()
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
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        var plugin = factory.plugin();
        factory.superParent()
            .plugin(plugin, Map.of("key", "parent-value"))
            .install(path);

        module.build(
            factory.conveyorJson()
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
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        var plugin = factory.plugin();
        factory.superParent()
            .plugin(plugin, Map.of("parent-key", "value"))
            .install(path);

        module.build(
            factory.conveyorJson()
                .plugin(plugin, Map.of("child-key", "value"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .containsIgnoringNewLines("parent-key=value", "child-key=value");
    }

    @Test
    void givenParentHasDependencies_whenBuild_thenChildInheritedDependencies(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent()
            .dependency(
                factory.dependency()
                    .name("implementation"),
                DependencyScope.IMPLEMENTATION
            )
            .dependency(
                factory.dependency()
                    .name("test"),
                DependencyScope.TEST
            )
            .install(path);

        module.build(
            factory.conveyorJson()
                .plugin(factory.plugin())
                .install(path),
            Stage.COMPILE
        );

        assertThat(modulePath(defaultBuildDirectory(path).resolve("plugin-1-module-path-implementation")))
            .containsExactly(path.resolve("implementation-1.jar"));
        assertThat(modulePath(defaultBuildDirectory(path).resolve("plugin-1-module-path-test")))
            .containsExactly(path.resolve("test-1.jar"));
    }

    @Test
    void givenChildHasDifferentProperty_whenBuild_thenPropertiesAreMerged(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent()
            .property("parent-key", "parent-value")
            .install(path);

        module.build(
            factory.conveyorJson()
                .property("child-key", "child-value")
                .plugin(
                    factory.plugin(),
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
    void givenChildHasPropertyWithSameKey_whenBuild_thenChildPropertyValueTookPrecedence(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent()
            .property("key", "parent-value")
            .install(path);

        module.build(
            factory.conveyorJson()
                .property("key", "child-value")
                .plugin(
                    factory.plugin(),
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
    void givenChildHasMultipleParents_whenBuild_thenChildInheritsFromAllParents(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent()
            .property("key", "value")
            .install(path);

        var buildFiles = module.build(
            factory.conveyorJson()
                .parent(
                    factory.project("parent")
                        .parent(
                            factory.project("grand-parent")
                                .plugin(
                                    factory.plugin(),
                                    Map.of("key", "${key}")
                                )
                        )
                        .dependency(factory.dependency())
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
