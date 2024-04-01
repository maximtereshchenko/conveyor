package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class TasksFeatureTests extends ConveyorTest {

    @Test
    void givenTaskBoundToLowerThanTargetStage_whenConstructToStage_thenTaskWasExecuted(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.TEST
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenTaskBoundToTargetStage_whenConstructToStage_thenTaskWasExecuted(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenTaskBoundToGreaterThanTargetStage_whenConstructToStage_thenTaskWasNotExecuted(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("instant", "1.0.0", Map.of())
                .install(path),
            Stage.CLEAN
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).doesNotExist();
    }

    @Test
    void givenTasksBoundToDifferentStages_whenConstructToStage_thenTasksWereExecutedInStageAscendingOrder(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version("1.0.0"))
            .jar("dependencies", builder -> builder.name("dependencies").version("1.0.0"))
            .manual(builder -> builder.name("dependency").version("1.0.0"))
            .jar("dependency", builder -> builder.name("dependency").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("dependencies", "1.0.0", Map.of())
                .dependency("dependency", "1.0.0", DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenSchematicRequireTransitiveDependency_whenConstructToStage_thenTransitiveDependencyIsUsedInTask(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version("1.0.0"))
            .jar("dependencies", builder -> builder.name("dependencies").version("1.0.0"))
            .manual(builder ->
                builder.name("dependency")
                    .version("1.0.0")
                    .dependency("transitive", "1.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("dependency").version("1.0.0"))
            .manual(builder -> builder.name("transitive").version("1.0.0"))
            .jar("dependency", builder -> builder.name("transitive").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("dependencies", "1.0.0", Map.of())
                .dependency("dependency", "1.0.0", DependencyScope.IMPLEMENTATION)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version("1.0.0"))
            .jar("dependencies", builder -> builder.name("dependencies").version("1.0.0"))
            .manual(builder -> builder.name("test").version("1.0.0"))
            .jar("dependency", builder -> builder.name("test").version("1.0.0"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", path, true)
                .plugin("dependencies", "1.0.0", Map.of("scope", "TEST"))
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("products").version("1.0.0"))
            .jar("products", builder -> builder.name("products").version("1.0.0"))
            .manual(builder -> builder.name("product").version("1.0.0"))
            .jar("product", builder -> builder.name("product").version("1.0.0"))
            .install(path);
        var product = path.resolve("product");
        var schematicDefinition = factory.schematicBuilder()
            .name("project")
            .version("1.0.0")
            .repository("main", path, true)
            .plugin("product", "1.0.0", Map.of("path", product.toString()))
            .plugin("products", "1.0.0", Map.of())
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
