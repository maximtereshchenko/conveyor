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
}
