package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.BuildFile;
import com.github.maximtereshchenko.conveyor.common.api.BuildFileType;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ModulePathTests extends ConveyorTest {

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
    void givenDependenciesWithDifferentScope_whenBuild_thenDependenciesFilteredByScope(
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
}
