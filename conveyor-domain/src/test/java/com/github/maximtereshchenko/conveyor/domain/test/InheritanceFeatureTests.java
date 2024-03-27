package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class InheritanceFeatureTests extends ConveyorTest {

    @Test
    void givenManualTemplate_whenConstructToStage_thenSchematicInheritsProperties(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
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
                factory.jarBuilder("properties")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository(path)
                .plugin("properties")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(6)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.schematic.version=1",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path),
                "conveyor.repository.remote.cache.directory=" + defaultCacheDirectory(path),
                "template.key=value"
            );
    }

    @Test
    void givenManualTemplate_whenConstructToStage_thenSchematicInheritsPlugins(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .plugin(
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
                factory.jarBuilder("instant")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository(path)
                .install(path),
            Stage.COMPILE
        );

        assertThat(instantPath(path)).exists();
    }

    @Test
    void givenManualTemplate_whenConstructToStage_thenSchematicInheritsDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
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
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository(path)
                .plugin("dependencies")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenManualTemplate_whenConstructToStage_thenSchematicInheritsTestDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .dependency(
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
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository(path)
                .plugin("dependencies", "1.0.0", Map.of("scope", "TEST"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsProperties(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("properties")
            )
            .jar(
                factory.jarBuilder("properties")
            )
            .install(path);
        var project = path.resolve("project");
        var templateSchematic = factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(conveyorJson(project))
            .property("template.key", "value")
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template(templateSchematic)
                .plugin("properties")
                .install(project),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("properties"))
            .content()
            .hasLineCount(6)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.schematic.version=1",
                "conveyor.discovery.directory=" + project,
                "conveyor.construction.directory=" + defaultConstructionDirectory(project),
                "conveyor.repository.remote.cache.directory=" + defaultCacheDirectory(path),
                "template.key=value"
            );
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsPlugins(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);
        var project = path.resolve("project");
        var templateSchematic = factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(conveyorJson(project))
            .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template(templateSchematic)
                .install(project),
            Stage.COMPILE
        );

        assertThat(instantPath(project)).exists();
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(path);
        var project = path.resolve("project");
        var templateSchematic = factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(conveyorJson(project))
            .dependency("dependency")
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template(templateSchematic)
                .plugin("dependencies")
                .install(project),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsTestDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(path);
        var project = path.resolve("project");
        var templateSchematic = factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(conveyorJson(project))
            .dependency("dependency", "1.0.0", DependencyScope.TEST)
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template(templateSchematic)
                .plugin("dependencies", "1.0.0", Map.of("scope", "TEST"))
                .install(project),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsRepository(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        var repository = path.resolve("repository");
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(repository);
        var project = path.resolve("project");
        var templateSchematic = factory.schematicDefinitionBuilder()
            .name("template")
            .repository(repository)
            .inclusion(conveyorJson(project))
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template(templateSchematic)
                .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                .install(project),
            Stage.COMPILE
        );

        assertThat(instantPath(project)).exists();
    }

    @Test
    void givenNoExplicitlyDefinedTemplate_whenConstructToStage_thenSchematicInheritsFromSchematicInParentDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);
        var project = path.resolve("project");
        factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(conveyorJson(project))
            .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .install(project),
            Stage.COMPILE
        );

        assertThat(instantPath(project)).exists();
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsProperties(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("properties")
            )
            .jar(
                factory.jarBuilder("properties")
            )
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("included")
                        .plugin("properties")
                        .install(included)
                )
                .property("template.key", "value")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(included).resolve("properties"))
            .content()
            .hasLineCount(6)
            .contains(
                "conveyor.schematic.name=included",
                "conveyor.schematic.version=1",
                "conveyor.discovery.directory=" + included,
                "conveyor.construction.directory=" + defaultConstructionDirectory(included),
                "conveyor.repository.remote.cache.directory=" + defaultCacheDirectory(path),
                "template.key=value"
            );
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsPlugins(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("template")
                        .install(included)
                )
                .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(instantPath(included)).exists();
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("included")
                        .plugin("dependencies")
                        .install(included)
                )
                .dependency("dependency")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(included).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsTestDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("included")
                        .plugin(
                            "dependencies",
                            "1.0.0",
                            Map.of("scope", "TEST")
                        )
                        .install(included)
                )
                .dependency("dependency", "1.0.0", DependencyScope.TEST)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(included).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsRepository(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        var repository = path.resolve("repository");
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(repository);
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(repository)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("included")
                        .plugin(
                            "instant",
                            "1.0.0",
                            Map.of("instant", "COMPILE-RUN")
                        )
                        .install(included)
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(instantPath(included)).exists();
    }

    @Test
    void givenSchematicWithInclusions_whenConstructToStage_thenSchematicIsConstructedBeforeItsInclusion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("included")
                        .install(included)
                )
                .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(constructed(path)).isBefore(constructed(included));
    }

    @Test
    void givenSchematicDependsOnOtherSchematic_whenConstructToStage_thenOtherSchematicIsConstructedBeforeThisSchematic(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("product")
            )
            .jar(
                factory.jarBuilder("product")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .install(path);
        var depends = path.resolve("depends");
        var dependency = path.resolve("dependency");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("depends")
                        .schematicDependency("dependency")
                        .install(depends)
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("dependency")
                        .plugin(
                            "product",
                            "1.0.0",
                            Map.of("path", path.resolve("dependency-1.0.0").toString())
                        )
                        .install(dependency)
                )
                .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.ARCHIVE
        );

        assertThat(constructed(dependency)).isBefore(constructed(depends));
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicIsConstructedAfterItsTemplate(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);
        var included = path.resolve("included");
        var templateSchematic = factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .inclusion(conveyorJson(included))
            .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("included")
                .template(templateSchematic)
                .install(included),
            Stage.COMPILE
        );

        assertThat(constructed(included)).isAfter(constructed(path));
    }

    @Test
    void givenSchematicTree_whenConstructToStage_thenSchematicsAreConstructedInDepthFirstOrder(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);
        var project = path.resolve("project");
        var projectDepth1a = project.resolve("project-depth-1a");
        var projectDepth1b = project.resolve("project-depth-1b");
        var projectDepth2a = projectDepth1a.resolve("project-depth-2a");
        var projectDepth2b = projectDepth1b.resolve("project-depth-2b");
        var projectSchematic = factory.schematicDefinitionBuilder()
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("project-depth-1a")
                    .inclusion(
                        factory.schematicDefinitionBuilder()
                            .name("project-depth-2a")
                            .install(projectDepth2a)
                    )
                    .install(projectDepth1a)
            )
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("project-depth-1b")
                    .inclusion(
                        factory.schematicDefinitionBuilder()
                            .name("project-depth-2b")
                            .install(projectDepth2b)
                    )
                    .install(projectDepth1b)
            )
            .install(project);
        factory.schematicDefinitionBuilder()
            .name("template")
            .repository(path)
            .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
            .inclusion(projectSchematic)
            .install(path);

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
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("product")
            )
            .jar(
                factory.jarBuilder("product")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(path);
        var depends = path.resolve("depends");
        var dependsSchematic = factory.schematicDefinitionBuilder()
            .name("depends")
            .plugin("dependencies")
            .schematicDependency("dependency")
            .install(depends);
        factory.schematicDefinitionBuilder()
            .repository(path)
            .inclusion(dependsSchematic)
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .plugin(
                        "product",
                        "1.0.0",
                        Map.of(
                            "path",
                            path.resolve("com")
                                .resolve("github")
                                .resolve("maximtereshchenko")
                                .resolve("conveyor")
                                .resolve("dependency")
                                .resolve("1.0.0")
                                .resolve("dependency-1.0.0.jar")
                                .toString()
                        )
                    )
                    .install(path.resolve("dependency"))
            )
            .install(path);

        module.construct(dependsSchematic, Stage.ARCHIVE);

        assertThat(defaultConstructionDirectory(depends).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenSchematicDoesNotRequireOtherSchematic_whenConstructToStage_thenOtherSchematicIsNotConstructed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);
        var unrelated = path.resolve("unrelated");
        var includedSchematic = factory.schematicDefinitionBuilder()
            .name("included")
            .install(path.resolve("included"));
        factory.schematicDefinitionBuilder()
            .repository(path)
            .inclusion(includedSchematic)
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("unrelated")
                    .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                    .install(unrelated)
            )
            .install(path);

        module.construct(includedSchematic, Stage.COMPILE);

        assertThat(instantPath(unrelated)).doesNotExist();
    }

    @Test
    void givenSchematicRequiresOtherSchematicTransitively_whenConstructToStage_thenOtherSchematicIsConstructed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);
        var transitive = path.resolve("transitive");
        var includedSchematic = factory.schematicDefinitionBuilder()
            .name("included")
            .schematicDependency("dependency")
            .install(path.resolve("included"));
        factory.schematicDefinitionBuilder()
            .repository(path)
            .inclusion(includedSchematic)
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
                    .schematicDependency("transitive")
                    .install(path.resolve("dependency"))
            )
            .inclusion(
                factory.schematicDefinitionBuilder()
                    .name("transitive")
                    .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                    .install(transitive)
            )
            .install(path);

        module.construct(includedSchematic, Stage.COMPILE);

        assertThat(instantPath(transitive)).exists();
    }

    @Test
    void givenStageIsLessThanArchive_whenConstructToStage_thenSchematicDependencyConstructedToArchive(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);
        var depends = path.resolve("depends");
        var dependency = path.resolve("dependency");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("depends")
                        .schematicDependency("dependency")
                        .install(depends)
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("dependency")
                        .install(dependency)
                )
                .plugin("instant", "1.0.0", Map.of("instant", "ARCHIVE-RUN"))
                .install(path),
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
    ) {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path);
        var depends = path.resolve("depends");
        var dependency = path.resolve("dependency");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("depends")
                        .schematicDependency("dependency")
                        .install(depends)
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("dependency")
                        .install(dependency)
                )
                .plugin("instant", "1.0.0", Map.of("instant", "PUBLISH-RUN"))
                .install(path),
            Stage.PUBLISH
        );

        assertThat(instantPath(path)).exists();
        assertThat(instantPath(depends)).exists();
        assertThat(instantPath(dependency)).exists();
    }

    private Instant constructed(Path path) {
        return instant(instantPath(path));
    }

    private Path instantPath(Path path) {
        return defaultConstructionDirectory(path).resolve("instant");
    }
}
