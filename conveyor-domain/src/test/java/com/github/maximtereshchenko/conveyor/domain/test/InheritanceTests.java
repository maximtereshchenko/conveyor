package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class InheritanceTests extends ConveyorTest {

    @Test
    void givenNoParentDeclared_whenBuild_thenProjectInheritedPluginsFromSuperParent(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual(builder -> builder.plugin("plugin", 1, Map.of()))
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        var projectBuildFiles = module.construct(
            factory.schematicBuilder()
                .repository(path)
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", ProductType.MODULE_COMPONENT))
            .contains(defaultConstructionDirectory(path).resolve("plugin-run"));
    }

    @Test
    void givenPluginDeclaredInSubprojectWithDifferentVersion_whenBuild_thenPluginVersionInSubprojectShouldTakePrecedence(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual(builder -> builder.plugin("plugin", 2, Map.of()))
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        var projectBuildFiles = module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("plugin", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", ProductType.MODULE_COMPONENT))
            .contains(defaultConstructionDirectory(path).resolve("plugin-run"));
    }

    @Test
    void givenPluginDeclaredInSubprojectWithDifferentConfigurationValue_whenBuild_thenValueInSubprojectShouldTakePrecedence(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual(builder -> builder.plugin("plugin", 1, Map.of("key", "parent-value")))
            .manual(builder -> builder.name("plugin").version(1))
            .jar("configuration-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("plugin", 1, Map.of("key", "value"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content(StandardCharsets.UTF_8)
            .contains("key=value");
    }

    @Test
    void givenPluginDeclaredInSubprojectWithAdditionalConfiguration_whenBuild_thenPluginConfigurationsAreMerged(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual(builder -> builder.plugin("plugin", 1, Map.of("parent-key", "value")))
            .manual(builder -> builder.name("plugin").version(1))
            .jar("configuration-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("plugin", 1, Map.of("key", "value"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content(StandardCharsets.UTF_8)
            .contains("parent-key=value", "key=value");
    }

    @Test
    void givenParentHasDependencies_whenBuild_thenSubprojectInheritedDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual(builder ->
                builder.dependency("implementation", 1, DependencyScope.IMPLEMENTATION)
                    .dependency("test", 1, DependencyScope.TEST)
            )
            .manual(builder -> builder.name("implementation").version(1))
            .emptyJar("implementation", 1)
            .manual(builder -> builder.name("test").version(1))
            .emptyJar("test", 1)
            .manual(builder -> builder.name("plugin").version(1))
            .jar("dependencies-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .plugin("plugin", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies-implementation"))
            .content(StandardCharsets.UTF_8)
            .contains(path.resolve("implementation-1.jar").toString());
        assertThat(defaultConstructionDirectory(path).resolve("dependencies-test"))
            .content(StandardCharsets.UTF_8)
            .contains(path.resolve("test-1.jar").toString());
    }

    @Test
    void givenSubprojectHasDifferentProperty_whenBuild_thenPropertiesAreMerged(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual(builder -> builder.property("parent-key", "parent-value"))
            .manual(builder -> builder.name("plugin").version(1))
            .jar("configuration-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .property("key", "value")
                .plugin(
                    "plugin",
                    1,
                    Map.of(
                        "parent-key", "${parent-key}",
                        "key", "${key}"
                    )
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content(StandardCharsets.UTF_8)
            .contains("parent-key=parent-value", "key=value");
    }

    @Test
    void givenSubprojectHasPropertyWithSameKey_whenBuild_thenSubprojectPropertyValueTookPrecedence(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual(builder -> builder.property("key", "parent-value"))
            .manual(builder -> builder.name("plugin").version(1))
            .jar("configuration-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .property("key", "value")
                .plugin("plugin", 1, Map.of("key", "${key}"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content(StandardCharsets.UTF_8)
            .contains("key=value");
    }

    @Test
    void givenProjectHierarchy_whenBuild_thenSubprojectInheritsFromAllParents(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual(builder -> builder.property("key", "value"))
            .manual(builder ->
                builder.name("parent")
                    .version(1)
                    .template("grand-parent", 1)
                    .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .manual(builder ->
                builder.name("grand-parent")
                    .version(1)
                    .plugin("configuration", 1, Map.of("key", "${key}"))
            )
            .manual(builder -> builder.name("dependency").version(1))
            .emptyJar("dependency", 1)
            .manual(builder -> builder.name("configuration").version(1))
            .jar("configuration-conveyor-plugin", builder -> builder.name("configuration").version(1))
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies-conveyor-plugin", builder -> builder.name("dependencies").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .repository(path)
                .template("parent", 1)
                .plugin("dependencies", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content(StandardCharsets.UTF_8)
            .contains("key=value");
        assertThat(defaultConstructionDirectory(path).resolve("dependencies-implementation"))
            .content(StandardCharsets.UTF_8)
            .contains(path.resolve("dependency-1.jar").toString());
    }

    @Test
    void givenSubproject_whenBuild_thenParentBuildBeforeSubproject(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);
        var subproject = path.resolve("subproject");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .repository(path)
                .inclusion(
                    factory.schematicBuilder()
                        .name("subproject")
                        .repository(path)
                        .install(subproject)
                )
                .plugin("plugin", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(instant(defaultConstructionDirectory(path).resolve("plugin-run")))
            .isBefore(instant(defaultConstructionDirectory(subproject).resolve("plugin-run")));
    }

    @Test
    void givenMultipleSubprojects_whenBuild_thenSubprojectsBuiltInOrder(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);
        var subproject1 = path.resolve("subproject-1");
        var subproject1a = subproject1.resolve("subproject-1-a");
        var subproject1b = subproject1.resolve("subproject-1-b");
        var subproject2 = path.resolve("subproject-2");
        var subproject2a = subproject2.resolve("subproject-2-a");
        var subproject2b = subproject2.resolve("subproject-2-b");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .repository(path)
                .inclusion(
                    factory.schematicBuilder()
                        .name("subproject-1")
                        .repository(path)
                        .inclusion(
                            factory.schematicBuilder()
                                .name("subproject-1-a")
                                .repository(path)
                                .install(subproject1a)
                        )
                        .inclusion(
                            factory.schematicBuilder()
                                .name("subproject-1-b")
                                .repository(path)
                                .install(subproject1b)
                        )
                        .install(subproject1)
                )
                .inclusion(
                    factory.schematicBuilder()
                        .name("subproject-2")
                        .repository(path)
                        .inclusion(
                            factory.schematicBuilder()
                                .name("subproject-2-a")
                                .repository(path)
                                .install(subproject2a)
                        )
                        .inclusion(
                            factory.schematicBuilder()
                                .name("subproject-2-b")
                                .repository(path)
                                .install(subproject2b)
                        )
                        .install(subproject2)
                )
                .plugin("plugin", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(
            List.of(
                instant(defaultConstructionDirectory(path).resolve("plugin-run")),
                instant(defaultConstructionDirectory(subproject1).resolve("plugin-run")),
                instant(defaultConstructionDirectory(subproject1a).resolve("plugin-run")),
                instant(defaultConstructionDirectory(subproject1b).resolve("plugin-run")),
                instant(defaultConstructionDirectory(subproject2).resolve("plugin-run")),
                instant(defaultConstructionDirectory(subproject2a).resolve("plugin-run")),
                instant(defaultConstructionDirectory(subproject2b).resolve("plugin-run"))
            )
        )
            .isSorted();
    }

    @Test
    void givenProjectDependsOnOtherProject_whenBuild_thenDependantProjectIsBuildAfterItsDependency(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("plugin").version(1))
            .jar("instant-conveyor-plugin", builder -> builder.name("plugin").version(1))
            .install(path);
        var subproject1 = path.resolve("subproject-1");
        var subproject2 = path.resolve("subproject-2");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .repository(path)
                .plugin("plugin", 1, Map.of())
                .inclusion(
                    factory.schematicBuilder()
                        .name("subproject-1")
                        .repository(path)
                        .schematicDependency("subproject-2", DependencyScope.IMPLEMENTATION)
                        .install(subproject1)
                )
                .inclusion(
                    factory.schematicBuilder()
                        .name("subproject-2")
                        .repository(path)
                        .install(subproject2)
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(
            List.of(
                instant(defaultConstructionDirectory(path).resolve("plugin-run")),
                instant(defaultConstructionDirectory(subproject2).resolve("plugin-run")),
                instant(defaultConstructionDirectory(subproject1).resolve("plugin-run"))
            )
        )
            .isSorted();
    }

    @Test
    void givenProjectDependsOnOtherProject_whenBuild_thenDependantProjectHasAccessToClassesFromDependency(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies-conveyor-plugin", builder -> builder.name("dependencies").version(1))
            .manual(builder -> builder.name("directory").version(1))
            .jar("discovery-directory-conveyor-plugin", builder -> builder.name("directory").version(1))
            .install(path);
        var dependant = path.resolve("dependant");
        var dependency = path.resolve("dependency");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .repository(path)
                .inclusion(
                    factory.schematicBuilder()
                        .name("dependency")
                        .repository(path)
                        .plugin("directory", 1, Map.of())
                        .install(dependency)
                )
                .inclusion(
                    factory.schematicBuilder()
                        .name("dependant")
                        .repository(path)
                        .schematicDependency("dependency", DependencyScope.IMPLEMENTATION)
                        .plugin("dependencies", 1, Map.of())
                        .install(dependant)
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(dependant).resolve("dependencies-implementation"))
            .content(StandardCharsets.UTF_8)
            .contains(dependency.toString());
    }
}
