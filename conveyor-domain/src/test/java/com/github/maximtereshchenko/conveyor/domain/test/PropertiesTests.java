package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class PropertiesTests extends ConveyorTest {

    @Test
    void givenProjectDirectoryProperty_whenBuild_thenProjectBuiltInSpecifiedDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);
        var project = path.resolve("project");

        var projectBuildFiles = module.construct(
            factory.schematicBuilder()
                .repository(path)
                .property("conveyor.discovery.directory", project.toString())
                .plugin("plugin", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", ProductType.MODULE_COMPONENT))
            .contains(defaultConstructionDirectory(project).resolve("plugin-run"));
    }

    @Test
    void givenRelativeProjectDirectoryProperty_whenBuild_thenProjectDirectoryIsRelativeToWorkingDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);
        var project = path.resolve("project");

        var projectBuildFiles = module.construct(
            factory.schematicBuilder()
                .repository(path)
                .property("conveyor.discovery.directory", path.relativize(project).toString())
                .plugin("plugin", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", ProductType.MODULE_COMPONENT))
            .contains(defaultConstructionDirectory(project).resolve("plugin-run"));
    }

    @Test
    void givenProjectBuildDirectoryProperty_whenBuild_thenProjectBuiltInSpecifiedDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);
        var build = path.resolve("build");

        var projectBuildFiles = module.construct(
            factory.schematicBuilder()
                .repository(path)
                .property("conveyor.construction.directory", build.toString())
                .plugin("plugin", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", ProductType.MODULE_COMPONENT))
            .contains(build.resolve("plugin-run"));
    }

    @Test
    void givenRelativeProjectBuildDirectoryProperty_whenBuild_thenProjectBuildDirectoryIsRelativeToProjectDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);
        var project = path.resolve("project");

        var projectBuildFiles = module.construct(
            factory.schematicBuilder()
                .repository(path)
                .property("conveyor.discovery.directory", project.toString())
                .property("conveyor.construction.directory", "./build")
                .plugin("plugin", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", ProductType.MODULE_COMPONENT))
            .contains(project.resolve("build").resolve("plugin-run"));
    }

    @Test
    void givenProperty_whenBuild_thenPropertyInterpolatedIntoPluginConfiguration(
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
                .property("property", "value")
                .plugin("plugin", 1, Map.of("property", "${property}-suffix"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("property=value-suffix");
    }
}
