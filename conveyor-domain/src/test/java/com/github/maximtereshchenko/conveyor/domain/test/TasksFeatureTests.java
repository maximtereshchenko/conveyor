package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
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
            .manual(builder -> builder.name("construction-directory").version(1))
            .jar("construction-directory", builder -> builder.name("construction-directory").version(1))
            .install(path);

        var schematicProducts = module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("construction-directory", 1, Map.of())
                .install(path),
            Stage.TEST
        );

        assertThat(schematicProducts.byType("project", ProductType.MODULE)).hasSize(1);
    }

    @Test
    void givenTaskBoundToTargetStage_whenConstructToStage_thenTaskWasExecuted(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("construction-directory").version(1))
            .jar("construction-directory", builder -> builder.name("construction-directory").version(1))
            .install(path);

        var schematicProducts = module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("construction-directory", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(schematicProducts.byType("project", ProductType.MODULE)).hasSize(1);
    }

    @Test
    void givenTaskBoundToGreaterThanTargetStage_whenConstructToStage_thenTaskWasNotExecuted(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("construction-directory").version(1))
            .jar("construction-directory", builder -> builder.name("construction-directory").version(1))
            .install(path);

        var schematicProducts = module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("construction-directory", 1, Map.of())
                .install(path),
            Stage.CLEAN
        );

        assertThat(schematicProducts.byType("project", ProductType.MODULE)).isEmpty();
    }

    @Test
    void givenTasksBoundToDifferentStages_whenConstructToStage_thenTasksWereExecutedInStageAscendingOrder(
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
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("dependencies", 1, Map.of())
                .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1");
    }

    @Test
    void givenSchematicRequireTransitiveDependency_whenConstructToStage_thenTransitiveDependencyIsUsedInTask(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
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
                .plugin("dependencies", 1, Map.of())
                .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("dependency-1", "transitive-1");
    }

    @Test
    void givenSchematicRequireTestDependency_whenConstructToStage_thenTestDependencyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .manual(builder -> builder.name("test").version(1))
            .jar("dependency", builder -> builder.name("test").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("dependencies", 1, Map.of("scope", "TEST"))
                .dependency("test", 1, DependencyScope.TEST)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("test-1");
    }

    @Test
    void givenTestDependencyRequireOtherDependencyHigherVersion_whenConstructToStage_thenOtherDependencyIsUsedWithLowerVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
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
                .plugin("dependencies", 1, Map.of())
                .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
                .dependency("test", 1, DependencyScope.TEST)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1");
    }

    @Test
    void givenSchematicRequiresCommonDependency_whenConstructToStage_thenDependencyIsUsedWithHighestVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .manual(builder ->
                builder.name("first")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("first").version(1))
            .manual(builder ->
                builder.name("second")
                    .version(1)
                    .dependency("dependency", 2, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("second").version(1))
            .manual(builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("dependency").version(2))
            .jar("dependency", builder -> builder.name("dependency").version(2))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .plugin("dependencies", 1, Map.of())
                .dependency("first", 1, DependencyScope.IMPLEMENTATION)
                .dependency("second", 1, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(3)
            .contains("first-1", "second-1", "dependency-2");
    }

    @Test
    void givenHighestDependencyVersionRequiredByExcludedDependency_whenConstructToStage_thenDependencyIsUsedWithLowerVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .manual(builder ->
                builder.name("first")
                    .version(1)
                    .dependency("should-not-be-affected", 1, DependencyScope.IMPLEMENTATION)
                    .dependency("exclude-affecting", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("first").version(1))
            .manual(builder ->
                builder.name("second")
                    .version(1)
                    .dependency("will-affect", 1, DependencyScope.IMPLEMENTATION)
            )
            .jar("dependency", builder -> builder.name("second").version(1))
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
                .plugin("dependencies", 1, Map.of())
                .dependency("first", 1, DependencyScope.IMPLEMENTATION)
                .dependency("second", 1, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(5)
            .contains("first-1", "second-1", "should-not-be-affected-1", "exclude-affecting-1", "will-affect-2");
    }

    @Test
    void givenPreviousTaskProducedProduct_whenConstructToStage_thenProductsAreUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("construction-directory").version(1))
            .jar("construction-directory", builder -> builder.name("construction-directory").version(1))
            .manual(builder -> builder.name("products").version(1))
            .jar("products", builder -> builder.name("products").version(1))
            .install(path);
        var schematicDefinition = factory.schematicBuilder()
            .name("project")
            .version(1)
            .repository(path)
            .plugin("construction-directory", 1, Map.of())
            .plugin("products", 1, Map.of())
            .install(path);

        module.construct(schematicDefinition, Stage.PUBLISH);

        assertThat(defaultConstructionDirectory(path).resolve("products"))
            .content()
            .hasLineCount(2)
            .contains(
                "SCHEMATIC_DEFINITION=" + schematicDefinition,
                "MODULE=" + defaultConstructionDirectory(path)
            );
    }

    private Instant instant(Path path, String fileName) {
        return instant(defaultConstructionDirectory(path).resolve(fileName));
    }
}
