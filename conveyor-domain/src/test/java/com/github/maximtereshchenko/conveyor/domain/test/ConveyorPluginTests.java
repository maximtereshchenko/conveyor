package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.BuildFile;
import com.github.maximtereshchenko.conveyor.common.api.BuildFileType;
import com.github.maximtereshchenko.conveyor.common.api.BuildFiles;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ConveyorPluginTests extends ConveyorTest {

    @Test
    void givenNoConveyorPluginsDeclared_whenBuild_thenNoBuildFiles(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        assertThat(module.build(factory.conveyorJson().install(path), Stage.COMPILE))
            .isEqualTo(new BuildFiles());
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
                    .plugin(factory.plugin())
                    .install(path),
                Stage.COMPILE
            )
        )
            .isEqualTo(
                new BuildFiles(
                    new BuildFile(
                        defaultBuildDirectory(path).resolve("project-plugin-1-prepared"),
                        BuildFileType.ARTIFACT
                    ),
                    new BuildFile(
                        defaultBuildDirectory(path).resolve("project-plugin-1-run"),
                        BuildFileType.ARTIFACT
                    ),
                    new BuildFile(
                        defaultBuildDirectory(path).resolve("project-plugin-1-finalized"),
                        BuildFileType.ARTIFACT
                    )
                )
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
                    .plugin(factory.plugin())
                    .install(path),
                Stage.CLEAN
            )
        )
            .isEqualTo(new BuildFiles());
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
                    factory.plugin(),
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
                .plugin(factory.plugin())
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
                    factory.plugin()
                        .name("clean")
                        .stage(Stage.CLEAN)
                )
                .plugin(
                    factory.plugin()
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
