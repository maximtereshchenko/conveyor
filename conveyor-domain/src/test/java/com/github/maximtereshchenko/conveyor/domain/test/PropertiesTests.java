package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.BuildFileType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class PropertiesTests extends ConveyorTest {

    @Test
    void givenProjectDirectoryProperty_whenBuild_thenProjectBuiltInSpecifiedDirectory(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    )
        throws Exception {
        factory.superParent().install(path);
        var project = Files.createDirectory(path.resolve("project"));

        var projectBuildFiles = module.build(
            factory.conveyorJson()
                .property("conveyor.project.directory", project.toString())
                .plugin(factory.pluginBuilder())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", BuildFileType.ARTIFACT))
            .contains(defaultBuildDirectory(project).resolve("project-plugin-1-run"));
    }

    @Test
    void givenRelativeProjectDirectoryProperty_whenBuild_thenProjectDirectoryIsRelativeToWorkingDirectory(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) throws Exception {
        factory.superParent().install(path);
        var project = Files.createDirectory(path.resolve("project"));

        var projectBuildFiles = module.build(
            factory.conveyorJson()
                .property(
                    "conveyor.project.directory",
                    Paths.get("").toAbsolutePath().relativize(project).toString()
                )
                .plugin(factory.pluginBuilder())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", BuildFileType.ARTIFACT))
            .contains(defaultBuildDirectory(project).resolve("project-plugin-1-run"));
    }

    @Test
    void givenProjectBuildDirectoryProperty_whenBuild_thenProjectBuiltInSpecifiedDirectory(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);
        var build = path.resolve("build");

        var projectBuildFiles = module.build(
            factory.conveyorJson()
                .property("conveyor.project.build.directory", build.toString())
                .plugin(factory.pluginBuilder())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", BuildFileType.ARTIFACT))
            .contains(build.resolve("project-plugin-1-run"));
    }

    @Test
    void givenRelativeProjectBuildDirectoryProperty_whenBuild_thenProjectBuildDirectoryIsRelativeToProjectDirectory(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);
        var project = path.resolve("project");

        var projectBuildFiles = module.build(
            factory.conveyorJson()
                .property("conveyor.project.directory", project.toString())
                .property("conveyor.project.build.directory", "./build")
                .plugin(factory.pluginBuilder())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", BuildFileType.ARTIFACT))
            .contains(project.resolve("build").resolve("project-plugin-1-run"));
    }

    @Test
    void givenProperty_whenBuild_thenPropertyInterpolatedIntoPluginConfiguration(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);

        module.build(
            factory.conveyorJson()
                .property("property", "value")
                .plugin(
                    factory.pluginBuilder(),
                    Map.of("property", "${property}-suffix")
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultBuildDirectory(path).resolve("project-plugin-1-configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("property=value-suffix");
    }
}
