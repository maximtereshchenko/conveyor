package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class InheritanceFeatureTests extends ConveyorTest {

    @Test
    void givenRemoteTemplate_whenConstructToStage_thenSchematicInheritsProperties(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .property("template.key", "value")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("properties")
            )
            .jar(
                factory.jarBuilder("properties", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository(path)
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "template.key")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .isEqualTo("template.key=value");
    }

    @Test
    void givenRemoteTemplate_whenConstructToStage_thenSchematicInheritsPlugins(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .plugin(
                        "group",
                        "instant",
                        "1.0.0",
                        Map.of("instant", "COMPILE-RUN")
                    )
            )
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
                .template("template")
                .repository(path)
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(instantPath(path)).exists();
    }

    @Test
    void givenRemoteTemplate_whenConstructToStage_thenSchematicInheritsDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .dependency("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository(path)
                .plugin("dependencies")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenRemoteTemplate_whenConstructToStage_thenSchematicInheritsTestDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .dependency(
                        "group",
                        "dependency",
                        "1.0.0",
                        DependencyScope.TEST
                    )
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository(path)
                .plugin(
                    "group",
                    "dependencies",
                    "1.0.0",
                    Map.of("scope", "TEST")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsProperties(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("properties")
            )
            .jar(
                factory.jarBuilder("properties", path)
            )
            .install(path);
        var project = path.resolve("project");
        factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(conveyorJson(project))
            .property("template.key", "value")
            .conveyorJson(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "template.key")
                )
                .conveyorJson(project),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("properties"))
            .content()
            .isEqualTo("template.key=value");
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsPlugins(
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
        var project = path.resolve("project");
        factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(conveyorJson(project))
            .plugin(
                "group",
                "instant",
                "1.0.0",
                Map.of("instant", "COMPILE-RUN")
            )
            .conveyorJson(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .conveyorJson(project),
            Stage.COMPILE
        );

        assertThat(instantPath(project)).exists();
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);
        var project = path.resolve("project");
        factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(conveyorJson(project))
            .dependency("dependency")
            .conveyorJson(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .plugin("dependencies")
                .conveyorJson(project),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsTestDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);
        var project = path.resolve("project");
        factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(conveyorJson(project))
            .dependency(
                "group",
                "dependency",
                "1.0.0",
                DependencyScope.TEST
            )
            .conveyorJson(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .plugin(
                    "group",
                    "dependencies",
                    "1.0.0",
                    Map.of("scope", "TEST")
                )
                .conveyorJson(project),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsRepository(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        var repository = path.resolve("repository");
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant", path)
            )
            .install(repository);
        var project = path.resolve("project");
        factory.schematicDefinitionBuilder()
            .name("template")
            .repository(repository)
            .inclusion(conveyorJson(project))
            .conveyorJson(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(project),
            Stage.COMPILE
        );

        assertThat(instantPath(project)).exists();
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsProperties(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("properties")
            )
            .jar(
                factory.jarBuilder("properties", path)
            )
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("included")
                        .template("template")
                        .plugin(
                            "group",
                            "properties",
                            "1.0.0",
                            Map.of("keys", "template.key")
                        )
                        .conveyorJson(included)
                )
                .property("template.key", "value")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(included).resolve("properties"))
            .content()
            .isEqualTo("template.key=value");
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsPlugins(
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
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("included")
                        .template("template")
                        .conveyorJson(included)
                )
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(instantPath(included)).exists();
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("included")
                        .template("template")
                        .plugin("dependencies")
                        .conveyorJson(included)
                )
                .dependency("dependency")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(included).resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsTestDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("included")
                        .template("template")
                        .plugin(
                            "group",
                            "dependencies",
                            "1.0.0",
                            Map.of("scope", "TEST")
                        )
                        .conveyorJson(included)
                )
                .dependency(
                    "group",
                    "dependency",
                    "1.0.0",
                    DependencyScope.TEST
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(included).resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsRepository(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        var repository = path.resolve("repository");
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant", path)
            )
            .install(repository);
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(repository)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("included")
                        .template("template")
                        .plugin(
                            "group",
                            "instant",
                            "1.0.0",
                            Map.of("instant", "COMPILE-RUN")
                        )
                        .conveyorJson(included)
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(instantPath(included)).exists();
    }

    @Test
    void givenSchematicWithInclusions_whenConstructToStage_thenSchematicIsConstructedBeforeItsInclusion(
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
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("included")
                        .template("template")
                        .conveyorJson(included)
                )
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(constructed(path)).isBefore(constructed(included));
    }

    @Test
    void givenSchematicDependsOnOtherSchematic_whenConstructToStage_thenOtherSchematicIsConstructedBeforeThisSchematic(
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
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("product")
            )
            .jar(
                factory.jarBuilder("product", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .install(path);
        var depends = path.resolve("depends");
        var dependency = path.resolve("dependency");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("depends")
                        .template("template")
                        .dependency("dependency")
                        .conveyorJson(depends)
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("dependency")
                        .template("template")
                        .plugin(
                            "group",
                            "product",
                            "1.0.0",
                            Map.of(
                                "path",
                                path.resolve("group")
                                    .resolve("dependency")
                                    .resolve("1.0.0")
                                    .resolve("dependency-1.0.0.jar")
                                    .toString()
                            )
                        )
                        .conveyorJson(dependency)
                )
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.ARCHIVE
        );

        assertThat(constructed(dependency)).isBefore(constructed(depends));
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicIsConstructedAfterItsTemplate(
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
        var included = path.resolve("included");
        factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(conveyorJson(included))
            .plugin(
                "group",
                "instant",
                "1.0.0",
                Map.of("instant", "COMPILE-RUN")
            )
            .conveyorJson(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("included")
                .template("template")
                .conveyorJson(included),
            Stage.COMPILE
        );

        assertThat(constructed(included)).isAfter(constructed(path));
    }

    @Test
    void givenSchematicTree_whenConstructToStage_thenSchematicsAreConstructedInDepthFirstOrder(
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
        var project = path.resolve("project");
        var projectDepth1a = project.resolve("project-depth-1a");
        var projectDepth1b = project.resolve("project-depth-1b");
        var projectDepth2a = projectDepth1a.resolve("project-depth-2a");
        var projectDepth2b = projectDepth1b.resolve("project-depth-2b");
        var projectSchematic = factory.schematicDefinitionBuilder()
            .name("project")
            .template("template")
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("project-depth-1a")
                    .template("project")
                    .inclusion(
                        factory.schematicDefinitionBuilder()
                            .name("project-depth-2a")
                            .template("project-depth-1a")
                            .conveyorJson(projectDepth2a)
                    )
                    .conveyorJson(projectDepth1a)
            )
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("project-depth-1b")
                    .template("project")
                    .inclusion(
                        factory.schematicDefinitionBuilder()
                            .name("project-depth-2b")
                            .template("project-depth-1b")
                            .conveyorJson(projectDepth2b)
                    )
                    .conveyorJson(projectDepth1b)
            )
            .conveyorJson(project);
        factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .plugin(
                "group",
                "instant",
                "1.0.0",
                Map.of("instant", "COMPILE-RUN")
            )
            .inclusion(projectSchematic)
            .conveyorJson(path);

        module.construct(projectSchematic, Stage.COMPILE);

        assertThat(
            Stream.of(path, project, projectDepth1a, projectDepth2a, projectDepth1b, projectDepth2b)
                .map(this::constructed)
        )
            .isSorted();
    }

    @Test
    void givenSchematicRequireOtherSchematic_whenConstructToStage_thenSchematicsConstructedTogether(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("product")
            )
            .jar(
                factory.jarBuilder("product", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .install(path);
        var depends = path.resolve("depends");
        var dependsSchematic = factory.schematicDefinitionBuilder()
            .name("depends")
            .template("template")
            .plugin("dependencies")
            .dependency("dependency")
            .conveyorJson(depends);
        factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(dependsSchematic)
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .template("template")
                    .plugin(
                        "group",
                        "product",
                        "1.0.0",
                        Map.of(
                            "path",
                            path.resolve("group")
                                .resolve("dependency")
                                .resolve("1.0.0")
                                .resolve("dependency-1.0.0.jar")
                                .toString()
                        )
                    )
                    .conveyorJson(path.resolve("dependency"))
            )
            .conveyorJson(path);

        module.construct(dependsSchematic, Stage.ARCHIVE);

        assertThat(defaultConstructionDirectory(depends).resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenSchematicDoesNotRequireOtherSchematic_whenConstructToStage_thenOtherSchematicIsNotConstructed(
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
        var unrelated = path.resolve("unrelated");
        var includedSchematic = factory.schematicDefinitionBuilder()
            .name("included")
            .conveyorJson(path.resolve("included"));
        factory.schematicDefinitionBuilder()
            .repository(path)
            .inclusion(includedSchematic)
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("unrelated")
                    .plugin(
                        "group",
                        "instant",
                        "1.0.0",
                        Map.of("instant", "COMPILE-RUN")
                    )
                    .conveyorJson(unrelated)
            )
            .conveyorJson(path);

        module.construct(includedSchematic, Stage.COMPILE);

        assertThat(instantPath(unrelated)).doesNotExist();
    }

    @Test
    void givenSchematicRequiresOtherSchematicTransitively_whenConstructToStage_thenOtherSchematicIsConstructed(
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
        var transitive = path.resolve("transitive");
        var includedSchematic = factory.schematicDefinitionBuilder()
            .name("included")
            .template("template")
            .dependency("dependency")
            .conveyorJson(path.resolve("included"));
        factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(includedSchematic)
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .template("template")
                    .dependency("transitive")
                    .conveyorJson(path.resolve("dependency"))
            )
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("transitive")
                    .template("template")
                    .plugin(
                        "group",
                        "instant",
                        "1.0.0",
                        Map.of("instant", "COMPILE-RUN")
                    )
                    .conveyorJson(transitive)
            )
            .conveyorJson(path);

        module.construct(includedSchematic, Stage.COMPILE);

        assertThat(instantPath(transitive)).exists();
    }

    @Test
    void givenStageIsLessThanArchive_whenConstructToStage_thenSchematicDependencyConstructedToArchive(
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
        var depends = path.resolve("depends");
        var dependency = path.resolve("dependency");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("depends")
                        .template("template")
                        .dependency("dependency")
                        .conveyorJson(depends)
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("dependency")
                        .template("template")
                        .conveyorJson(dependency)
                )
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "ARCHIVE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(instantPath(path)).doesNotExist();
        assertThat(instantPath(depends)).doesNotExist();
        assertThat(instantPath(dependency)).exists();
    }

    @Test
    void givenStageIsGreaterThanArchive_whenConstructToStage_thenAllSchematicsAreConstructedToStage(
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
        var depends = path.resolve("depends");
        var dependency = path.resolve("dependency");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("depends")
                        .template("template")
                        .dependency("dependency")
                        .conveyorJson(depends)
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("dependency")
                        .template("template")
                        .conveyorJson(dependency)
                )
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "PUBLISH-RUN")
                )
                .conveyorJson(path),
            Stage.PUBLISH
        );

        assertThat(instantPath(path)).exists();
        assertThat(instantPath(depends)).exists();
        assertThat(instantPath(dependency)).exists();
    }

    @Test
    void givenRelativeInclusion_whenConstructToStage_thenInclusionResolvedRelativeToSchematicDefinitionDirectory(
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
        var inclusion = path.resolve("inclusion");
        factory.schematicDefinitionBuilder()
            .name("inclusion")
            .template("template")
            .property(
                "conveyor.schematic.template.location",
                "../template/conveyor.json"
            )
            .plugin(
                "group",
                "instant",
                "1.0.0",
                Map.of("instant", "COMPILE-RUN")
            )
            .conveyorJson(inclusion);

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(path)
                .inclusion(
                    path.getFileSystem()
                        .getPath("..", "inclusion", "conveyor.json")
                )
                .conveyorJson(path.resolve("template")),
            Stage.COMPILE
        );

        assertThat(instantPath(inclusion)).exists();
    }

    @Test
    void givenCommonTransitiveSchematicDependency_whenConstructToStage_thenSchematicsAreBuiltInCorrectOrder(
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
        var dependsTransitively = path.resolve("depends-transitively");
        var intermediate = path.resolve("intermediate");
        var dependsOnCommon = path.resolve("depends-on-common");
        var common = path.resolve("common");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .template("template")
                        .name("depends-transitively")
                        .dependency("intermediate")
                        .conveyorJson(dependsTransitively)
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .template("template")
                        .name("depends-on-common")
                        .dependency("common")
                        .conveyorJson(dependsOnCommon)
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .template("template")
                        .name("intermediate")
                        .dependency("common")
                        .conveyorJson(intermediate)
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .template("template")
                        .name("common")
                        .conveyorJson(common)
                )
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(
            Stream.of(path, common, intermediate, dependsTransitively, dependsOnCommon)
                .map(this::constructed)
        )
            .isSorted();
    }

    private Instant constructed(Path path) {
        try {
            return instant(instantPath(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path instantPath(Path path) {
        return defaultConstructionDirectory(path).resolve("instant");
    }
}
