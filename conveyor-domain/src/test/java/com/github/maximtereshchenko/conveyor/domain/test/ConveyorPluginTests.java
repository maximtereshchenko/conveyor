package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class ConveyorPluginTests extends ConveyorTest {

    @Test
    void givenNoConveyorPluginsDeclared_whenBuild_thenNoBuildFiles(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .install(path);

        assertThat(
            module.construct(
                    factory.schematicBuilder()
                        .repository(path)
                        .install(path),
                    Stage.COMPILE
                )
                .byType("project", ProductType.MODULE_COMPONENT)
        )
            .isEmpty();
    }

    @Test
    void givenConveyorPluginDeclared_whenBuild_thenTaskFromPluginExecuted(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        assertThat(
            module.construct(
                    factory.schematicBuilder()
                        .repository(path)
                        .plugin("plugin", 1, Map.of())
                        .install(path),
                    Stage.COMPILE
                )
                .byType("project", ProductType.MODULE_COMPONENT)
        )
            .contains(defaultConstructionDirectory(path).resolve("plugin-run"));
    }

    @Test
    void givenTaskBindToCompileStage_whenBuildUntilCleanStage_thenTaskDidNotExecuted(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        assertThat(
            module.construct(
                    factory.schematicBuilder()
                        .repository(path)
                        .plugin("plugin", 1, Map.of())
                        .install(path),
                    Stage.CLEAN
                )
                .byType("project", ProductType.MODULE_COMPONENT)
        )
            .isEmpty();
    }

    @Test
    void givenPluginConfiguration_whenBuild_thenPluginCanSeeItsConfiguration(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("configuration-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("plugin", 1, Map.of("property", "value"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content(StandardCharsets.UTF_8)
            .contains("property=value");
    }

    @Test
    void givenPluginDeclared_whenBuild_thenTasksShouldRunInStepOrder(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("plugin", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(
            List.of(
                instant(defaultConstructionDirectory(path).resolve("plugin-prepare")),
                instant(defaultConstructionDirectory(path).resolve("plugin-run")),
                instant(defaultConstructionDirectory(path).resolve("plugin-finalize"))
            )
        )
            .isSorted();

    }

    @Test
    void givenMultiplePlugins_whenBuild_thenTasksShouldRunInStageOrder(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("clean").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("clean").version(1))
            .manual(builder -> builder.name("compile").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("compile").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("clean", 1, Map.of("stage", "CLEAN"))
                .plugin("compile", 1, Map.of("stage", "COMPILE"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(
            List.of(
                instant(defaultConstructionDirectory(path).resolve("clean-prepare")),
                instant(defaultConstructionDirectory(path).resolve("clean-run")),
                instant(defaultConstructionDirectory(path).resolve("clean-finalize")),
                instant(defaultConstructionDirectory(path).resolve("compile-prepare")),
                instant(defaultConstructionDirectory(path).resolve("compile-run")),
                instant(defaultConstructionDirectory(path).resolve("compile-finalize"))
            )
        )
            .isSorted();
    }
}
