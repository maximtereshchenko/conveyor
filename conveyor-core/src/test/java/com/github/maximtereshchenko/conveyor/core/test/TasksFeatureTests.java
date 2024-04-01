package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class TasksFeatureTests extends ConveyorTest {

    @Test
    void givenTaskBoundToLowerThanTargetStage_whenConstructToStage_thenTaskWasExecuted(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.TEST
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenTaskBoundToTargetStage_whenConstructToStage_thenTaskWasExecuted(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenTaskBoundToGreaterThanTargetStage_whenConstructToStage_thenTaskWasNotExecuted(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("instant")
                .install(path),
            Stage.CLEAN
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).doesNotExist();
    }

    @Test
    void givenTasksBoundToDifferentStages_whenConstructToStage_thenTasksWereExecutedInStageAscendingOrder(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin(
                    "instant",
                    "1.0.0",
                    Map.of(
                        "clean", "CLEAN-RUN",
                        "compile", "COMPILE-RUN",
                        "test", "TEST-RUN",
                        "archive", "ARCHIVE-RUN",
                        "publish", "PUBLISH-RUN"
                    )
                )
                .install(path),
            Stage.PUBLISH
        );

        assertThat(
            List.of(
                instant(path, "clean"),
                instant(path, "compile"),
                instant(path, "test"),
                instant(path, "archive"),
                instant(path, "publish")
            )
        )
            .isSorted();
    }

    @Test
    void givenTasksBoundToSameStage_whenConstructToStage_thenTasksWereExecutedInStepAscendingOrder(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin(
                    "instant",
                    "1.0.0",
                    Map.of(
                        "prepare", "COMPILE-PREPARE",
                        "run", "COMPILE-RUN",
                        "finalize", "COMPILE-FINALIZE"
                    )
                )
                .install(path),
            Stage.PUBLISH
        );

        assertThat(
            List.of(
                instant(path, "prepare"),
                instant(path, "run"),
                instant(path, "finalize")
            )
        )
            .isSorted();
    }

    @Test
    void givenSchematicRequiresDependency_whenConstructToStage_thenDependencyIsUsedInTask(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("dependencies")
                .dependency("dependency")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenSchematicRequireTransitiveDependency_whenConstructToStage_thenTransitiveDependencyIsUsedInTask(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
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
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("transitive")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("dependencies")
                .dependency("dependency")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("dependency-1.0.0", "transitive-1.0.0");
    }

    @Test
    void givenSchematicRequireTestDependency_whenConstructToStage_thenTestDependencyIsUsed(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("test")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("test")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin(
                    "dependencies",
                    "1.0.0",
                    Map.of("scope", "TEST")
                )
                .dependency("test", "1.0.0", DependencyScope.TEST)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("test-1.0.0");
    }

    @Test
    void givenPreviousTaskProducedProduct_whenConstructToStage_thenProductsAreUsed(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("products")
            )
            .jar(
                factory.jarBuilder("products")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("product")
            )
            .jar(
                factory.jarBuilder("product")
            )
            .install(path);
        var product = path.resolve("product");
        var schematicDefinition = factory.schematicDefinitionBuilder()
            .repository(path)
            .plugin("product", "1.0.0", Map.of("path", product.toString()))
            .plugin("products")
            .install(path);

        module.construct(schematicDefinition, Stage.PUBLISH);

        assertThat(defaultConstructionDirectory(path).resolve("products"))
            .content()
            .hasLineCount(2)
            .contains("SCHEMATIC_DEFINITION=" + schematicDefinition, "MODULE=" + product);
    }

    private Instant instant(Path path, String fileName) {
        return instant(defaultConstructionDirectory(path).resolve(fileName));
    }
}
