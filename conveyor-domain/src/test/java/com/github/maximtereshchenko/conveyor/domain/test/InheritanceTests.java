package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.BuildFile;
import com.github.maximtereshchenko.conveyor.common.api.BuildFileType;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class InheritanceTests extends ConveyorTest {

    @Test
    void givenNoParentDeclared_whenBuild_thenProjectInheritedPluginsFromSuperParent(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent()
            .plugin(factory.pluginBuilder())
            .install(path);

        var buildFiles = module.build(
            factory.conveyorJson().install(path),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(defaultBuildDirectory(path).resolve("project-plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenPluginDeclaredInSubprojectWithDifferentVersion_whenBuild_thenPluginVersionInSubprojectShouldTakePrecedence(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        var plugin = factory.pluginBuilder();
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
                new BuildFile(defaultBuildDirectory(path).resolve("project-plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenPluginDeclaredInSubprojectWithDifferentConfigurationValue_whenBuild_thenValueInSubprojectShouldTakePrecedence(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        var plugin = factory.pluginBuilder();
        factory.superParent()
            .plugin(plugin, Map.of("key", "parent-value"))
            .install(path);

        module.build(
            factory.conveyorJson()
                .plugin(plugin, Map.of("key", "value"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("project-plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("key=value");
    }

    @Test
    void givenPluginDeclaredInSubprojectWithAdditionalConfiguration_whenBuild_thenPluginConfigurationsAreMerged(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        var plugin = factory.pluginBuilder();
        factory.superParent()
            .plugin(plugin, Map.of("parent-key", "value"))
            .install(path);

        module.build(
            factory.conveyorJson()
                .plugin(plugin, Map.of("key", "value"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("project-plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .containsIgnoringNewLines("parent-key=value", "key=value");
    }

    @Test
    void givenParentHasDependencies_whenBuild_thenSubprojectInheritedDependencies(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent()
            .dependency(
                factory.dependencyBuilder()
                    .name("implementation"),
                DependencyScope.IMPLEMENTATION
            )
            .dependency(
                factory.dependencyBuilder()
                    .name("test"),
                DependencyScope.TEST
            )
            .install(path);

        module.build(
            factory.conveyorJson()
                .plugin(factory.pluginBuilder())
                .install(path),
            Stage.COMPILE
        );

        assertThat(modulePath(defaultBuildDirectory(path).resolve("project-plugin-1-module-path-implementation")))
            .containsExactly(path.resolve("implementation-1.jar"));
        assertThat(modulePath(defaultBuildDirectory(path).resolve("project-plugin-1-module-path-test")))
            .containsExactly(path.resolve("test-1.jar"));
    }

    @Test
    void givenSubprojectHasDifferentProperty_whenBuild_thenPropertiesAreMerged(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent()
            .property("parent-key", "parent-value")
            .install(path);

        module.build(
            factory.conveyorJson()
                .property("key", "value")
                .plugin(
                    factory.pluginBuilder(),
                    Map.of(
                        "parent-key", "${parent-key}",
                        "key", "${key}"
                    )
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("project-plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .containsIgnoringNewLines("parent-key=parent-value", "key=value");
    }

    @Test
    void givenSubprojectHasPropertyWithSameKey_whenBuild_thenSubprojectPropertyValueTookPrecedence(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent()
            .property("key", "parent-value")
            .install(path);

        module.build(
            factory.conveyorJson()
                .property("key", "value")
                .plugin(
                    factory.pluginBuilder(),
                    Map.of("key", "${key}")
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("project-plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("key=value");
    }

    @Test
    void givenProjectHierarchy_whenBuild_thenSubprojectInheritsFromAllParents(
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
                    factory.projectBuilder("parent")
                        .parent(
                            factory.projectBuilder("grand-parent")
                                .plugin(
                                    factory.pluginBuilder(),
                                    Map.of("key", "${key}")
                                )
                        )
                        .dependency(factory.dependencyBuilder())
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(defaultBuildDirectory(path).resolve("project-plugin-1-run"), BuildFileType.ARTIFACT)
            );
        assertThat(defaultBuildDirectory(path).resolve("project-plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("key=value");
        assertThat(modulePath(defaultBuildDirectory(path).resolve("project-plugin-1-module-path-implementation")))
            .containsExactly(path.resolve("dependency-1.jar"));
    }

    @Test
    void givenSubproject_whenBuild_thenParentBuildBeforeSubproject(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        module.build(
            factory.conveyorJson()
                .name("project")
                .subproject(
                    factory.conveyorJson()
                        .name("subproject")
                )
                .plugin(factory.pluginBuilder())
                .install(path),
            Stage.COMPILE
        );

        assertThat(instant(defaultBuildDirectory(path).resolve("project-plugin-1-run")))
            .isBefore(
                instant(
                    defaultBuildDirectory(path.resolve("subproject"))
                        .resolve("subproject-plugin-1-run")
                )
            );
    }
}
