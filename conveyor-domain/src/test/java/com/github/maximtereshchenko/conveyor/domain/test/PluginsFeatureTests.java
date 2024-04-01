package com.github.maximtereshchenko.conveyor.domain.test;

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
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("properties").version(1))
            .jar("properties", builder -> builder.name("properties").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .property("user.defined.property", "value")
                .plugin("properties", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(4)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path),
                "user.defined.property=value"
            );
    }

    @Test
    void givenProperty_whenConstructToStage_thenPropertyIsInterpolatedIntoPluginConfiguration(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("configuration").version(1))
            .jar("configuration", builder -> builder.name("configuration").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .property("property", "value")
                .plugin("configuration", 1, Map.of("configuration", "${property}-suffix"))
                .install(path),
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
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("configuration").version(1))
            .jar("configuration", builder -> builder.name("configuration").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("configuration", 1, Map.of("user.defined.configuration", "value"))
                .install(path),
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
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin(
                    "instant",
                    1,
                    Map.of(
                        "enabled", "false",
                        "instant", "COMPILE-RUN"
                    )
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).doesNotExist();
    }

    @Test
    void givenPluginRequiresDependency_whenConstructToStage_thenDependencyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("module-path")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("module-path").version(1))
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("module-path", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content()
            .isEqualTo("dependency-1");
    }

    @Test
    void givenPluginRequireTransitiveDependency_whenConstructToStage_thenTransitiveDependencyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("module-path")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("module-path").version(1))
            .manual(builder ->
                builder.name("dependency")
                    .version(1)
                    .dependency("transitive", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("transitive").version(1))
            .jar("dependency", builder -> builder.name("transitive").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("module-path", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(2)
            .contains("dependency-1", "transitive-1");
    }

    @Test
    void givenPluginManualDeclareTestDependency_whenConstructToStage_thenThatDependencyIsNotUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("module-path")
                    .version(1)
                    .dependency("test", 1, DependencyScope.TEST)
            )
            .jar("module-path", builder -> builder.name("module-path").version(1))
            .manual(builder -> builder.name("test").version(1))
            .jar("dependency", builder -> builder.name("test").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("module-path", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .isEmpty();
    }

    @Test
    void givenTestDependencyRequireHigherVersion_whenConstructToStage_thenDependencyIsUsedWithLowerVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("module-path")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
                    .dependency("test", 1, DependencyScope.TEST)
            )
            .jar("module-path", builder -> builder.name("module-path").version(1))
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder ->
                builder.name("test")
                    .version(1)
                    .dependency("dependency", 2, DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("dependency").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("module-path", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("dependency-1");
    }

    @Test
    void givenPluginsRequireCommonDependency_whenConstructToStage_thenDependencyIsUsedWithHighestVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("first")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("first").version(1))
            .manual(builder ->
                builder.name("second")
                    .version(1)
                    .dependency("dependency", 2, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("second").version(1))
            .manual(builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("dependency").version(2))
            .jar("dependency", builder -> builder.name("dependency").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("first", 1, Map.of())
                .plugin("second", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("dependency-2");
    }

    @Test
    void givenHighestDependencyVersionRequiredByExcludedDependency_whenConstructToStage_thenDependencyIsUsedWithLowerVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("first")
                    .version(1)
                    .dependency("should-not-be-affected", 1, DependencyScope.IMPLEMENTATION)
                    .dependency("exclude-affecting", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("first").version(1))
            .manual(builder ->
                builder.name("second")
                    .version(1)
                    .dependency("will-affect", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("second").version(1))
            .manual(builder -> builder.name("should-not-be-affected").version(1))
            .jar("dependency", builder -> builder.name("should-not-be-affected").version(1))
            .manual(builder ->
                builder.name("exclude-affecting")
                    .version(1)
                    .dependency("will-affect", 2, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("exclude-affecting").version(1))
            .manual(builder ->
                builder.name("will-affect")
                    .version(1)
                    .dependency("should-not-be-affected", 2, DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("should-not-be-affected").version(2))
            .manual(builder -> builder.name("will-affect").version(2))
            .jar("dependency", builder -> builder.name("will-affect").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("first", 1, Map.of())
                .plugin("second", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(3)
            .contains("should-not-be-affected-1", "exclude-affecting-1", "will-affect-2");
    }

    @Test
    void givenTemplateWithPlugins_whenConstructToStage_thenSchematicInheritsPlugins(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
            )
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .template("template", 1)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenSchematicDeclareInheritedPluginWithDifferentVersion_whenConstructToStage_thenOverriddenPluginVersionIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .plugin("instant", 1, Map.of())
            )
            .manual(builder -> builder.name("instant").version(1))
            .manual(builder -> builder.name("instant").version(2))
            .jar("instant", builder -> builder.name("instant").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .template("template", 1)
                .plugin("instant", 2, Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenSchematicDeclareInheritedPluginWithSameConfiguration_whenConstructToStage_thenOverriddenConfigurationIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .plugin("configuration", 1, Map.of("key", "template-value"))
            )
            .manual(builder -> builder.name("configuration").version(1))
            .jar("configuration", builder -> builder.name("configuration").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .template("template", 1)
                .plugin("configuration", Map.of("key", "schematic-value"))
                .install(path),
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
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .plugin("configuration", 1, Map.of("to.be.removed", "value"))
            )
            .manual(builder -> builder.name("configuration").version(1))
            .jar("configuration", builder -> builder.name("configuration").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .template("template", 1)
                .plugin("configuration", Map.of("to.be.removed", ""))
                .install(path),
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
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .plugin("configuration", 1, Map.of("inherited.key", "inherited.value"))
            )
            .manual(builder -> builder.name("configuration").version(1))
            .jar("configuration", builder -> builder.name("configuration").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .template("template", 1)
                .plugin("configuration", Map.of("key", "value"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content()
            .hasLineCount(3)
            .contains("enabled=true", "inherited.key=inherited.value", "key=value");
    }
}
