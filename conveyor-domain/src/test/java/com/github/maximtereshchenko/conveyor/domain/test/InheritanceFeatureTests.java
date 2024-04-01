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
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .property("template.key", "value")
            )
            .manual(builder -> builder.name("properties").version(1))
            .jar("properties", builder -> builder.name("properties").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .template("template", 1)
                .repository("main", path, true)
                .plugin("properties", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(4)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path),
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
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
            )
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .template("template", 1)
                .repository("main", path, true)
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
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .template("template", 1)
                .repository("main", path, true)
                .plugin("dependencies", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1");
    }

    @Test
    void givenManualTemplate_whenConstructToStage_thenSchematicInheritsTestDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .dependency("dependency", 1, DependencyScope.TEST)
            )
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .template("template", 1)
                .repository("main", path, true)
                .plugin("dependencies", 1, Map.of("scope", "TEST"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1");
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsProperties(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("properties").version(1))
            .jar("properties", builder -> builder.name("properties").version(1))
            .install(path);
        var project = path.resolve("project");
        var templateSchematic = factory.schematicBuilder()
            .name("template")
            .version(1)
            .repository("main", path, true)
            .inclusion(conveyorJson(project))
            .property("template.key", "value")
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .template(templateSchematic)
                .plugin("properties", 1, Map.of())
                .install(project),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("properties"))
            .content()
            .hasLineCount(4)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.discovery.directory=" + project,
                "conveyor.construction.directory=" + defaultConstructionDirectory(project),
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
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);
        var project = path.resolve("project");
        var templateSchematic = factory.schematicBuilder()
            .name("template")
            .version(1)
            .repository("main", path, true)
            .inclusion(conveyorJson(project))
            .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
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
            .superManual()
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .install(path);
        var project = path.resolve("project");
        var templateSchematic = factory.schematicBuilder()
            .name("template")
            .version(1)
            .repository("main", path, true)
            .inclusion(conveyorJson(project))
            .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .template(templateSchematic)
                .plugin("dependencies", 1, Map.of())
                .install(project),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1");
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsTestDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .install(path);
        var project = path.resolve("project");
        var templateSchematic = factory.schematicBuilder()
            .name("template")
            .version(1)
            .repository("main", path, true)
            .inclusion(conveyorJson(project))
            .dependency("dependency", 1, DependencyScope.TEST)
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .template(templateSchematic)
                .plugin("dependencies", 1, Map.of("scope", "TEST"))
                .install(project),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1");
    }

    @Test
    void givenSchematicTemplate_whenConstructToStage_thenSchematicInheritsRepository(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        var repository = path.resolve("repository");
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(repository);
        var project = path.resolve("project");
        var templateSchematic = factory.schematicBuilder()
            .name("template")
            .version(1)
            .repository("main", repository, true)
            .inclusion(conveyorJson(project))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .template(templateSchematic)
                .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
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
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);
        var project = path.resolve("project");
        factory.schematicBuilder()
            .name("template")
            .version(1)
            .repository("main", path, true)
            .inclusion(conveyorJson(project))
            .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .install(project),
            Stage.COMPILE
        );

        assertThat(instantPath(project)).exists();
    }

    @Test
    void givenNoExplicitlyDefinedTemplate_whenConstructToStage_thenSchematicInheritsFromSuperManual(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual(builder -> builder.plugin("instant", 1, Map.of("instant", "COMPILE-RUN")))
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .install(path),
            Stage.COMPILE
        );

        assertThat(instantPath(path)).exists();
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsProperties(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("properties").version(1))
            .jar("properties", builder -> builder.name("properties").version(1))
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("included")
                        .version(1)
                        .plugin("properties", 1, Map.of())
                        .install(included)
                )
                .property("template.key", "value")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(included).resolve("properties"))
            .content()
            .hasLineCount(4)
            .contains(
                "conveyor.schematic.name=included",
                "conveyor.discovery.directory=" + included,
                "conveyor.construction.directory=" + defaultConstructionDirectory(included),
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
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("template")
                        .version(1)
                        .install(included)
                )
                .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
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
            .superManual()
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("included")
                        .version(1)
                        .plugin("dependencies", 1, Map.of())
                        .install(included)
                )
                .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(included).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1");
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsTestDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("included")
                        .version(1)
                        .plugin("dependencies", 1, Map.of("scope", "TEST"))
                        .install(included)
                )
                .dependency("dependency", 1, DependencyScope.TEST)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(included).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1");
    }

    @Test
    void givenIncludedSchematic_whenConstructToStage_thenSchematicInheritsRepository(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        var repository = path.resolve("repository");
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(repository);
        var included = path.resolve("included");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", repository, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("included")
                        .version(1)
                        .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
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
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);
        var included = path.resolve("included");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("included")
                        .version(1)
                        .install(included)
                )
                .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
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
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .manual(builder -> builder.name("product").version(1))
            .jar("product", builder -> builder.name("product").version(1))
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .install(path);
        var depends = path.resolve("depends");
        var dependency = path.resolve("dependency");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("depends")
                        .version(1)
                        .schematicDependency("dependency", DependencyScope.IMPLEMENTATION)
                        .install(depends)
                )
                .inclusion(
                    factory.schematicBuilder()
                        .name("dependency")
                        .version(1)
                        .plugin(
                            "product",
                            1,
                            Map.of("path", path.resolve("dependency-1").toString())
                        )
                        .install(dependency)
                )
                .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
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
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);
        var included = path.resolve("included");
        var templateSchematic = factory.schematicBuilder()
            .name("template")
            .version(1)
            .repository("main", path, true)
            .inclusion(conveyorJson(included))
            .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("included")
                .version(1)
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
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);
        var project = path.resolve("project");
        var projectDepth1a = path.resolve("project-depth-1a");
        var projectDepth1b = path.resolve("project-depth-1b");
        var projectDepth2a = projectDepth1a.resolve("project-depth-2a");
        var projectDepth2b = projectDepth1b.resolve("project-depth-2b");
        var projectSchematic = factory.schematicBuilder()
            .name("project")
            .version(1)
            .inclusion(
                factory.schematicBuilder()
                    .name("project-depth-1a")
                    .version(1)
                    .inclusion(
                        factory.schematicBuilder()
                            .name("project-depth-2a")
                            .version(1)
                            .install(projectDepth2a)
                    )
                    .install(projectDepth1a)
            )
            .inclusion(
                factory.schematicBuilder()
                    .name("project-depth-1b")
                    .version(1)
                    .inclusion(
                        factory.schematicBuilder()
                            .name("project-depth-2b")
                            .version(1)
                            .install(projectDepth2b)
                    )
                    .install(projectDepth1b)
            )
            .install(project);
        factory.schematicBuilder()
            .name("template")
            .version(1)
            .repository("main", path, true)
            .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
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
            .superManual()
            .manual(builder -> builder.name("dependency").version(1))
            .jar("dependency", builder -> builder.name("dependency").version(1))
            .manual(builder -> builder.name("product").version(1))
            .jar("product", builder -> builder.name("product").version(1))
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .install(path);
        var depends = path.resolve("depends");
        var dependsSchematic = factory.schematicBuilder()
            .name("depends")
            .version(1)
            .plugin("dependencies", 1, Map.of())
            .schematicDependency("dependency", DependencyScope.IMPLEMENTATION)
            .install(depends);
        factory.schematicBuilder()
            .name("project")
            .version(1)
            .repository("main", path, true)
            .inclusion(dependsSchematic)
            .inclusion(
                factory.schematicBuilder()
                    .name("dependency")
                    .version(1)
                    .plugin(
                        "product",
                        1,
                        Map.of("path", path.resolve("dependency-1.jar").toString())
                    )
                    .install(path.resolve("dependency"))
            )
            .install(path);

        module.construct(dependsSchematic, Stage.ARCHIVE);

        assertThat(defaultConstructionDirectory(depends).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1");
    }

    @Test
    void givenSchematicDoesNotRequireOtherSchematic_whenConstructToStage_thenOtherSchematicIsNotConstructed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);
        var unrelated = path.resolve("unrelated");
        var includedSchematic = factory.schematicBuilder()
            .name("included")
            .version(1)
            .install(path.resolve("included"));
        factory.schematicBuilder()
            .name("project")
            .version(1)
            .repository("main", path, true)
            .inclusion(includedSchematic)
            .inclusion(
                factory.schematicBuilder()
                    .name("unrelated")
                    .version(1)
                    .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
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
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);
        var transitive = path.resolve("transitive");
        var includedSchematic = factory.schematicBuilder()
            .name("included")
            .version(1)
            .schematicDependency("dependency", DependencyScope.IMPLEMENTATION)
            .install(path.resolve("included"));
        factory.schematicBuilder()
            .name("project")
            .version(1)
            .repository("main", path, true)
            .inclusion(includedSchematic)
            .inclusion(
                factory.schematicBuilder()
                    .name("dependency")
                    .version(1)
                    .schematicDependency("transitive", DependencyScope.IMPLEMENTATION)
                    .install(path.resolve("dependency"))
            )
            .inclusion(
                factory.schematicBuilder()
                    .name("transitive")
                    .version(1)
                    .plugin("instant", 1, Map.of("instant", "COMPILE-RUN"))
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
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);
        var depends = path.resolve("depends");
        var dependency = path.resolve("dependency");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("depends")
                        .version(1)
                        .schematicDependency("dependency", DependencyScope.IMPLEMENTATION)
                        .install(depends)
                )
                .inclusion(
                    factory.schematicBuilder()
                        .name("dependency")
                        .version(1)
                        .install(dependency)
                )
                .plugin("instant", 1, Map.of("instant", "ARCHIVE-RUN"))
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
            .superManual()
            .manual(builder -> builder.name("instant").version(1))
            .jar("instant", builder -> builder.name("instant").version(1))
            .install(path);
        var depends = path.resolve("depends");
        var dependency = path.resolve("dependency");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository("main", path, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("depends")
                        .version(1)
                        .schematicDependency("dependency", DependencyScope.IMPLEMENTATION)
                        .install(depends)
                )
                .inclusion(
                    factory.schematicBuilder()
                        .name("dependency")
                        .version(1)
                        .install(dependency)
                )
                .plugin("instant", 1, Map.of("instant", "PUBLISH-RUN"))
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
