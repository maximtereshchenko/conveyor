package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class DependenciesFeatureTests extends ConveyorTest {

    @Test
    void givenTemplateHasDifferentDependency_whenConstructToStage_thenSchematicHasBothDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
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
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("template-dependency-1.0.0", "schematic-dependency-1.0.0");
    }

    @Test
    void givenSchematicOverridesDependency_whenConstructToStage_thenVersionIsOverridden(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
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
                .dependency("dependency", "2.0.0", DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(1)
            .contains("dependency-2.0.0");
    }

    @Test
    void givenSchematicOverridesDependency_whenConstructToStage_thenScopeIsOverridden(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
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
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(1)
            .contains("dependency-1.0.0");
    }

    @Test
    void givenDependencyOnOtherSchematic_whenConstructToStage_thenModuleProductIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
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
                            "product",
                            "1.0.0",
                            Map.of(
                                "path",
                                path.resolve("com")
                                    .resolve("github")
                                    .resolve("maximtereshchenko")
                                    .resolve("conveyor")
                                    .resolve("first")
                                    .resolve("1.0.0")
                                    .resolve("first-1.0.0.jar")
                                    .toString()
                            )
                        )
                        .install(path.resolve("first"))
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("second")
                        .template("project")
                        .plugin("dependencies")
                        .dependency("first")
                        .install(second)
                )
                .install(path),
            Stage.ARCHIVE
        );

        assertThat(defaultConstructionDirectory(second).resolve("dependencies"))
            .content()
            .isEqualTo("first-1.0.0");
    }
}
