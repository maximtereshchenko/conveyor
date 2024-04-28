package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class PluginsFeatureTests extends ConveyorTest {

    @Test
    void givenPluginDefined_whenConstructToStage_thenPropertiesAreUsedInPlugin(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("properties")
            )
            .jar(
                factory.jarBuilder("properties", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("user.defined.property", "value")
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "user.defined.property")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .isEqualTo("user.defined.property=value");
    }

    @Test
    void givenProperty_whenConstructToStage_thenPropertyIsInterpolatedIntoPluginConfiguration(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("configuration")
            )
            .jar(
                factory.jarBuilder("configuration", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("property", "value")
                .plugin(
                    "group",
                    "configuration",
                    "1.0.0",
                    Map.of("configuration", "${property}-suffix")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content()
            .hasLineCount(2)
            .contains("enabled=true", "configuration=value-suffix");
    }

    @Test
    void givenPluginDefined_whenConstructToStage_thenConfigurationIsUsedInPlugin(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("configuration")
            )
            .jar(
                factory.jarBuilder("configuration", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin(
                    "group",
                    "configuration",
                    "1.0.0",
                    Map.of("user.defined.configuration", "value")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content()
            .hasLineCount(2)
            .contains(
                "enabled=true",
                "user.defined.configuration=value"
            );
    }

    @Test
    void givenPluginIsDisabled_whenConstructToStage_thenPluginTasksAreNotExecuted(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of(
                        "enabled", "false",
                        "instant", "COMPILE-RUN"
                    )
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).doesNotExist();
    }

    @Test
    void givenPluginRequiresDependency_whenConstructToStage_thenDependencyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("class-path")
                    .dependency("dependency")
            )
            .jar(
                factory.jarBuilder("class-path", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("class-path")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("class-path"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenPluginRequireTransitiveDependency_whenConstructToStage_thenTransitiveDependencyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("class-path")
                    .dependency("dependency")
            )
            .jar(
                factory.jarBuilder("class-path", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .dependency("transitive")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("transitive")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("transitive")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("class-path")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("class-path"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(2)
            .contains("group-dependency-1.0.0", "transitive-1.0.0");
    }

    @Test
    void givenPluginSchematicDeclareTestDependency_whenConstructToStage_thenThatDependencyIsNotUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("class-path")
                    .dependency(
                        "group",
                        "test",
                        "1.0.0",
                        DependencyScope.TEST
                    )
            )
            .jar(
                factory.jarBuilder("class-path", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("test")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("test")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("class-path")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("class-path"))
            .content(StandardCharsets.UTF_8)
            .isEmpty();
    }

    @Test
    void givenTemplateWithPlugins_whenConstructToStage_thenSchematicInheritsPlugins(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .plugin(
                        "group",
                        "instant",
                        "1.0.0",
                        Map.of("instant", "COMPILE-RUN")
                    )
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenSchematicDeclareInheritedPluginWithDifferentVersion_whenConstructToStage_thenOverriddenPluginVersionIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .plugin("instant")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
                    .version("2.0.0")
            )
            .jar(
                factory.jarBuilder("instant", path)
                    .version("2.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .plugin(
                    "group",
                    "instant",
                    "2.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenSchematicDeclareInheritedPluginWithSameConfiguration_whenConstructToStage_thenOverriddenConfigurationIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .plugin(
                        "group",
                        "configuration",
                        "1.0.0",
                        Map.of("key", "template-value")
                    )
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("configuration")
            )
            .jar(
                factory.jarBuilder("configuration", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .plugin(
                    "group",
                    "configuration",
                    null,
                    Map.of("key", "schematic-value")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content()
            .hasLineCount(2)
            .contains(
                "enabled=true",
                "key=schematic-value"
            );
    }

    @Test
    void givenSchematicDeclareInheritedPluginWithEmptyConfigurationValue_whenConstructToStage_thenConfigurationDoesNotContainKey(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .plugin(
                        "group",
                        "configuration",
                        "1.0.0",
                        Map.of("to.be.removed", "value")
                    )
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("configuration")
            )
            .jar(
                factory.jarBuilder("configuration", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .plugin(
                    "group",
                    "configuration",
                    null,
                    Map.of("to.be.removed", "")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content()
            .isEqualTo("enabled=true");
    }

    @Test
    void givenPluginConfiguration_whenConstructToStage_thenConfigurationIsMergedWithInherited(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .plugin(
                        "group",
                        "configuration",
                        "1.0.0",
                        Map.of("inherited.key", "inherited.value")
                    )
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("configuration")
            )
            .jar(
                factory.jarBuilder("configuration", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .plugin(
                    "group",
                    "configuration",
                    null,
                    Map.of("key", "value")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content()
            .hasLineCount(3)
            .contains("enabled=true", "inherited.key=inherited.value", "key=value");
    }
}
