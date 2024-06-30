package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
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
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.TEST
        );

        assertThat(path.resolve("instant")).exists();
    }

    @Test
    void givenTaskBoundToTargetStage_whenConstructToStage_thenTaskWasExecuted(
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
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(path.resolve("instant")).exists();
    }

    @Test
    void givenTaskBoundToGreaterThanTargetStage_whenConstructToStage_thenTaskWasNotExecuted(
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
                .plugin("instant")
                .conveyorJson(path),
            Stage.CLEAN
        );

        assertThat(path.resolve("instant")).doesNotExist();
    }

    @Test
    void givenTasksBoundToDifferentStages_whenConstructToStage_thenTasksWereExecutedInStageAscendingOrder(
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
                        "clean", "CLEAN-RUN",
                        "compile", "COMPILE-RUN",
                        "test", "TEST-RUN",
                        "archive", "ARCHIVE-RUN",
                        "publish", "PUBLISH-RUN"
                    )
                )
                .conveyorJson(path),
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
                        "prepare", "COMPILE-PREPARE",
                        "run", "COMPILE-RUN",
                        "finalize", "COMPILE-FINALIZE"
                    )
                )
                .conveyorJson(path),
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
    void givenTasksBoundToSameStageAndStep_whenConstructToStage_thenTasksWereExecutedInPluginsOrder(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("first")
            )
            .jar(
                factory.jarBuilder("instant", path)
                    .name("first")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("second")
            )
            .jar(
                factory.jarBuilder("instant", path)
                    .name("second")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin(
                    "group",
                    "second",
                    "1.0.0",
                    Map.of("second", "COMPILE-RUN")
                )
                .plugin(
                    "group",
                    "first",
                    "1.0.0",
                    Map.of("first", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.PUBLISH
        );

        assertThat(List.of(instant(path, "second"), instant(path, "first")))
            .isSorted();
    }

    @Test
    void givenSchematicRequiresDependency_whenConstructToStage_thenDependencyIsUsedInTask(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
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
                .plugin("dependencies")
                .dependency("dependency")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenSchematicRequireTransitiveDependency_whenConstructToStage_thenTransitiveDependencyIsUsedInTask(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
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
                .plugin("dependencies")
                .dependency("dependency")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("group-dependency-1.0.0", "transitive-1.0.0");
    }

    @Test
    void givenSchematicRequireTestDependency_whenConstructToStage_thenTestDependencyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
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
                .plugin(
                    "group",
                    "dependencies",
                    "1.0.0",
                    Map.of("scope", "TEST")
                )
                .dependency(
                    "group",
                    "test",
                    "1.0.0",
                    DependencyScope.TEST
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .isEqualTo("group-test-1.0.0");
    }

    @Test
    void givenCacheableTask_whenConstructToStage_thenTaskIsNotExecutedDuringNextConstruction(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("cache")
            )
            .jar(
                factory.jarBuilder("cache", path)
            )
            .install(path);
        var conveyorJson = factory.schematicDefinitionBuilder()
            .repository(path)
            .plugin("cache")
            .conveyorJson(path);
        var output = path.resolve("output");

        module.construct(conveyorJson, Stage.COMPILE);
        var instant = instant(output);
        module.construct(conveyorJson, Stage.COMPILE);

        assertThat(instant(output)).isEqualTo(instant);
    }

    @Test
    void givenCacheableTask_whenConstructToStage_thenCacheIsInDefaultDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("cache")
            )
            .jar(
                factory.jarBuilder("cache", path)
            )
            .install(path);

        module.construct(factory.schematicDefinitionBuilder()
            .repository(path)
            .plugin("cache")
            .conveyorJson(path), Stage.COMPILE);

        var cache = path.resolve(".conveyor-cache")
            .resolve("tasks")
            .resolve("group")
            .resolve("project")
            .resolve("cache");
        assertThat(cache.resolve("0").resolve("output")).exists();
        assertThat(cache.resolve("inputs")).exists();
        assertThat(cache.resolve("outputs")).exists();
    }

    @Test
    void givenTaskCacheDirectoryProperty_whenConstructToStage_thenCacheIsInSpecifiedDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("cache")
            )
            .jar(
                factory.jarBuilder("cache", path)
            )
            .install(path);
        var customCacheDirectory = path.resolve("custom-cache-directory");

        module.construct(factory.schematicDefinitionBuilder()
            .repository(path)
            .plugin("cache")
            .property("conveyor.tasks.cache.directory", customCacheDirectory.toString())
            .conveyorJson(path), Stage.COMPILE);

        var cache = customCacheDirectory.resolve("cache");
        assertThat(cache.resolve("0").resolve("output")).exists();
        assertThat(cache.resolve("inputs")).exists();
        assertThat(cache.resolve("outputs")).exists();
    }

    @Test
    void givenCacheableTaskInputChanged_whenConstructToStage_thenTaskExecutedDuringNextConstruction(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("cache")
            )
            .jar(
                factory.jarBuilder("cache", path)
            )
            .install(path);
        var conveyorJson = factory.schematicDefinitionBuilder()
            .repository(path)
            .plugin("cache")
            .conveyorJson(path);
        var output = path.resolve("output");

        module.construct(conveyorJson, Stage.COMPILE);
        var instant = instant(output);
        Files.writeString(path.resolve("input"), "changed");
        module.construct(conveyorJson, Stage.COMPILE);

        assertThat(instant(output)).isNotEqualTo(instant);
    }

    @Test
    void givenTaskCachedOutputs_whenConstructToStage_thenTaskIsNotExecutedAndOutputsAreCopied(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("cache")
            )
            .jar(
                factory.jarBuilder("cache", path)
            )
            .install(path);
        var conveyorJson = factory.schematicDefinitionBuilder()
            .repository(path)
            .plugin("cache")
            .conveyorJson(path);
        var output = path.resolve("output");

        module.construct(conveyorJson, Stage.COMPILE);
        var instant = instant(output);
        Files.writeString(output, "changed");
        module.construct(conveyorJson, Stage.COMPILE);

        assertThat(instant(output)).isEqualTo(instant);
    }

    @Test
    void givenCacheableTaskInputDirectoryWithDifferentlyNamedFiles_whenConstructToStage_thenTaskIsExecuted(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("cache")
            )
            .jar(
                factory.jarBuilder("cache", path)
            )
            .install(path);
        var conveyorJson = factory.schematicDefinitionBuilder()
            .repository(path)
            .plugin("cache")
            .conveyorJson(path);
        var output = path.resolve("output");
        var initial = Files.createFile(
            Files.createDirectories(path.resolve("input")).resolve("initial")
        );

        module.construct(conveyorJson, Stage.COMPILE);
        var instant = instant(output);
        Files.move(initial, initial.getParent().resolve("moved"));
        module.construct(conveyorJson, Stage.COMPILE);

        assertThat(instant(output)).isNotEqualTo(instant);
    }

    @Test
    void givenCacheableTaskInputConfigurationChanged_whenConstructToStage_thenTaskIsExecuted(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("cache")
            )
            .jar(
                factory.jarBuilder("cache", path)
            )
            .install(path);
        var output = path.resolve("output");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin("cache")
                .conveyorJson(path),
            Stage.COMPILE
        );
        var instant = instant(output);
        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .plugin(
                    "group",
                    "cache",
                    "1.0.0",
                    Map.of("input", "changed")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(instant(output)).isNotEqualTo(instant);
    }

    private Instant instant(Path path, String fileName) throws IOException {
        return instant(path.resolve(fileName));
    }
}
