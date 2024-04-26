package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.schematic.ExclusionDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class DependenciesFeatureTests extends ConveyorTest {

    @Test
    void givenTemplateHasDifferentDependency_whenConstructToStage_thenSchematicHasBothDependencies(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
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
                factory.jarBuilder("dependency")
                    .name("template-dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("schematic-dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("schematic-dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .plugin("dependencies")
                .dependency("schematic-dependency")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("template-dependency-1.0.0", "schematic-dependency-1.0.0");
    }

    @Test
    void givenSchematicOverridesDependency_whenConstructToStage_thenVersionIsOverridden(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
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
                factory.jarBuilder("dependency")
                    .version("2.0.0")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
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
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(1)
            .contains("dependency-2.0.0");
    }

    @Test
    void givenSchematicOverridesDependency_whenConstructToStage_thenScopeIsOverridden(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
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
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .plugin("dependencies")
                .dependency("dependency")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(1)
            .contains("dependency-1.0.0");
    }

    @Test
    void givenDependencyOnOtherSchematic_whenConstructToStage_thenModuleProductIsUsed(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("product")
            )
            .jar(
                factory.jarBuilder("product")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("first")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
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
                            "product",
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
            Stage.ARCHIVE
        );

        assertThat(defaultConstructionDirectory(second).resolve("dependencies"))
            .content()
            .isEqualTo("first-1.0.0");
    }

    @Test
    void givenExcludedDependency_whenConstructToStage_thenDependencyIsNotInModulePath(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .dependency("excluded")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("excluded")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("excluded")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
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
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenExcludedTransitiveDependency_whenConstructToStage_thenDependencyIsNotInModulePath(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .dependency("transitive")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("transitive")
                    .dependency("excluded")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("transitive")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("excluded")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("excluded")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
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
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("dependency-1.0.0", "transitive-1.0.0");
    }

    @Test
    void givenTransitivelyExcludedDependency_whenConstructToStage_thenDependencyIsNotInModulePath(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
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
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("transitive")
                    .dependency("excluded")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("transitive")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("excluded")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("excluded")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("dependencies")
                .dependency("dependency")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("dependency-1.0.0", "transitive-1.0.0");
    }
}
