package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.BuildFileType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class ConveyorPluginTests extends ConveyorTest {

    @Test
    void givenNoConveyorPluginsDeclared_whenBuild_thenNoBuildFiles(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        assertThat(
            module.build(factory.conveyorJson().install(path), Stage.COMPILE)
                .byType("project", BuildFileType.ARTIFACT)
        )
            .isEmpty();
    }

    @Test
    void givenConveyorPluginDeclared_whenBuild_thenTaskFromPluginExecuted(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        assertThat(
            module.build(
                    factory.conveyorJson()
                        .plugin(factory.pluginBuilder())
                        .install(path),
                    Stage.COMPILE
                )
                .byType("project", BuildFileType.ARTIFACT)
        )
            .contains(
                defaultBuildDirectory(path).resolve("project-plugin-1-prepared"),
                defaultBuildDirectory(path).resolve("project-plugin-1-run"),
                defaultBuildDirectory(path).resolve("project-plugin-1-finalized")
            );
    }

    @Test
    void givenTaskBindToCompileStage_whenBuildUntilCleanStage_thenTaskDidNotExecuted(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        assertThat(
            module.build(
                    factory.conveyorJson()
                        .plugin(factory.pluginBuilder())
                        .install(path),
                    Stage.CLEAN
                )
                .byType("project", BuildFileType.ARTIFACT)
        )
            .isEmpty();
    }

    @Test
    void givenPluginConfiguration_whenBuild_thenPluginCanSeeItsConfiguration(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        module.build(
            factory.conveyorJson()
                .plugin(
                    factory.pluginBuilder(),
                    Map.of("property", "value")
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("project-plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("property=value");
    }

    @Test
    void givenPluginDeclared_whenBuild_thenTasksShouldRunInStepOrder(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        module.build(
            factory.conveyorJson()
                .plugin(factory.pluginBuilder())
                .install(path),
            Stage.COMPILE
        );

        var preparedTime = instant(defaultBuildDirectory(path).resolve("project-plugin-1-prepared"));
        var runTime = instant(defaultBuildDirectory(path).resolve("project-plugin-1-run"));
        var finalizedTime = instant(defaultBuildDirectory(path).resolve("project-plugin-1-finalized"));
        assertThat(preparedTime).isBefore(runTime);
        assertThat(runTime).isBefore(finalizedTime);
    }

    @Test
    void givenMultiplePlugins_whenBuild_thenTasksShouldRunInStageOrder(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        module.build(
            factory.conveyorJson()
                .plugin(
                    factory.pluginBuilder()
                        .name("clean")
                        .stage(Stage.CLEAN)
                )
                .plugin(
                    factory.pluginBuilder()
                        .name("compile")
                        .stage(Stage.COMPILE)
                )
                .install(path),
            Stage.COMPILE
        );

        var cleanPreparedTime = instant(defaultBuildDirectory(path).resolve("project-clean-1-prepared"));
        var cleanRunTime = instant(defaultBuildDirectory(path).resolve("project-clean-1-run"));
        var cleanFinalizedTime = instant(defaultBuildDirectory(path).resolve("project-clean-1-finalized"));
        var compilePreparedTime = instant(defaultBuildDirectory(path).resolve("project-compile-1-prepared"));
        var compileRunTime = instant(defaultBuildDirectory(path).resolve("project-compile-1-run"));
        var compileFinalizedTime = instant(defaultBuildDirectory(path).resolve("project-compile-1-finalized"));
        assertThat(cleanPreparedTime).isBefore(cleanRunTime);
        assertThat(cleanRunTime).isBefore(cleanFinalizedTime);
        assertThat(compilePreparedTime).isBefore(compileRunTime);
        assertThat(compileRunTime).isBefore(compileFinalizedTime);
        assertThat(compilePreparedTime).isAfter(cleanFinalizedTime);
    }
}
