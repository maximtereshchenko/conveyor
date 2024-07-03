package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.schematic.ExclusionDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class DependenciesFeatureTests extends ConveyorTest {

    @Test
    void givenTemplateHasDifferentDependency_whenConstructToStage_thenSchematicHasBothDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .dependency("template-dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template-dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("template-dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("schematic-dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("schematic-dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .plugin("dependencies")
                .dependency("schematic-dependency")
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("group-template-dependency-1.0.0", "group-schematic-dependency-1.0.0");
    }

    @Test
    void givenSchematicOverridesDependency_whenConstructToStage_thenVersionIsOverridden(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .dependency("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .version("1.0.0")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .version("2.0.0")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .version("2.0.0")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .plugin("dependencies")
                .dependency(
                    "group",
                    "dependency",
                    "2.0.0",
                    DependencyScope.IMPLEMENTATION
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .hasLineCount(1)
            .contains("group-dependency-2.0.0");
    }

    @Test
    void givenSchematicOverridesDependency_whenConstructToStage_thenScopeIsOverridden(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .dependency("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .plugin("dependencies")
                .dependency("dependency")
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenDependencyOnOtherSchematic_whenConstructToStage_thenSchematicArtifactIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("artifact")
            )
            .jar(
                factory.jarBuilder("artifact", path)
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("first")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);
        var second = path.resolve("second");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("first")
                        .template("project")
                        .plugin(
                            "group",
                            "artifact",
                            "1.0.0",
                            Map.of(
                                "path",
                                path.resolve("group")
                                    .resolve("first")
                                    .resolve("1.0.0")
                                    .resolve("first-1.0.0.jar")
                                    .toString()
                            )
                        )
                        .conveyorJson(path.resolve("first"))
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("second")
                        .template("project")
                        .plugin("dependencies")
                        .dependency("first")
                        .conveyorJson(second)
                )
                .conveyorJson(path),
            List.of(Stage.ARCHIVE)
        );

        assertThat(second.resolve("dependencies"))
            .content()
            .isEqualTo("group-first-1.0.0");
    }

    @Test
    void givenExcludedDependency_whenConstructToStage_thenDependencyIsNotInClasspath(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .dependency("excluded")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("excluded")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("excluded")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("dependencies")
                .dependency(
                    "group",
                    "dependency",
                    "1.0.0",
                    DependencyScope.IMPLEMENTATION,
                    new ExclusionDefinition("group", "excluded")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenExcludedTransitiveDependency_whenConstructToStage_thenDependencyIsNotInClasspath(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                    .dependency("excluded")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("transitive")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("excluded")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("excluded")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("dependencies")
                .dependency(
                    "group",
                    "dependency",
                    "1.0.0",
                    DependencyScope.IMPLEMENTATION,
                    new ExclusionDefinition("group", "excluded")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("group-dependency-1.0.0", "transitive-1.0.0");
    }

    @Test
    void givenTransitivelyExcludedDependency_whenConstructToStage_thenDependencyIsNotInClasspath(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .dependency(
                        "group",
                        "transitive",
                        "1.0.0",
                        DependencyScope.IMPLEMENTATION,
                        new ExclusionDefinition("group", "excluded")
                    )
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("transitive")
                    .dependency("excluded")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("transitive")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("excluded")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("excluded")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("dependencies")
                .dependency("dependency")
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("group-dependency-1.0.0", "transitive-1.0.0");
    }
}
