package com.github.maximtereshchenko.conveyor.domain.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.BuildFile;
import com.github.maximtereshchenko.conveyor.common.api.BuildFileType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

        var buildFiles = module.build(
            factory.conveyorJson()
                .property("conveyor.project.directory", project.toString())
                .plugin(factory.plugin())
                .install(path),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(defaultBuildDirectory(project).resolve("project-plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenRelativeProjectDirectoryProperty_whenBuild_thenProjectDirectoryIsRelativeToWorkingDirectory(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) throws Exception {
        factory.superParent().install(path);
        var project = Files.createDirectory(path.resolve("project"));

        var buildFiles = module.build(
            factory.conveyorJson()
                .property(
                    "conveyor.project.directory",
                    Paths.get("").toAbsolutePath().relativize(project).toString()
                )
                .plugin(factory.plugin())
                .install(path),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(defaultBuildDirectory(project).resolve("project-plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenProjectBuildDirectoryProperty_whenBuild_thenProjectBuiltInSpecifiedDirectory(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);
        var build = path.resolve("build");

        var buildFiles = module.build(
            factory.conveyorJson()
                .property("conveyor.project.build.directory", build.toString())
                .plugin(factory.plugin())
                .install(path),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(build.resolve("project-plugin-1-run"), BuildFileType.ARTIFACT)
            );
    }

    @Test
    void givenRelativeProjectBuildDirectoryProperty_whenBuild_thenProjectBuildDirectoryIsRelativeToProjectDirectory(
        @TempDir Path path,
        ConveyorModule module,
        ArtifactFactory factory
    ) {
        factory.superParent().install(path);
        var project = path.resolve("project");

        var buildFiles = module.build(
            factory.conveyorJson()
                .property("conveyor.project.directory", project.toString())
                .property("conveyor.project.build.directory", "./build")
                .plugin(factory.plugin())
                .install(path),
            Stage.COMPILE
        );

        assertThat(buildFiles.byType(BuildFileType.ARTIFACT))
            .contains(
                new BuildFile(project.resolve("build").resolve("project-plugin-1-run"), BuildFileType.ARTIFACT)
            );
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
                    factory.plugin(),
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
