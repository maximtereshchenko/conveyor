package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.tomakehurst.wiremock.shadowed.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(WireMockExtension.class)
final class RepositoriesFeatureTests extends ConveyorTest {

    @Test
    void givenSchematicTemplateHasDifferentRepository_whenConstructToStage_thenSchematicHasBothRepositories(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        var first = path.resolve("first");
        var second = path.resolve("second");
        factory.repositoryBuilder()
            .install(first);
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(second);
        var project = path.resolve("project");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository("first", first, true)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("project")
                        .template("template")
                        .repository("second", second, true)
                        .plugin(
                            "group",
                            "instant",
                            "1.0.0",
                            Map.of("instant", "COMPILE-RUN")
                        )
                        .conveyorJson(project)
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("instant")).exists();
    }

    @Test
    void givenSchematicTemplateHasSameRepository_whenConstructToStage_thenRepositoryPathIsOverridden(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        var templateRepository = path.resolve("template-repository");
        factory.repositoryBuilder()
            .install(templateRepository);
        var projectRepository = path.resolve("project-repository");
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(projectRepository);
        var project = path.resolve("project");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository("main", templateRepository, true)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("project")
                        .template("template")
                        .repository("main", projectRepository, true)
                        .plugin(
                            "group",
                            "instant",
                            "1.0.0",
                            Map.of("instant", "COMPILE-RUN")
                        )
                        .conveyorJson(project)
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("instant")).exists();
    }

    @Test
    void givenSchematicTemplateHasSameRepository_whenConstructToStage_thenRepositoryEnabledFlagIsOverridden(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        var templateRepository = path.resolve("template-repository");
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("module-path")
            )
            .jar(
                factory.jarBuilder("module-path")
            )
            .install(templateRepository);
        var projectRepository = path.resolve("project-repository");
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("module-path")
                    .dependency("dependency")
            )
            .jar(
                factory.jarBuilder("module-path")
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .install(projectRepository);
        var project = path.resolve("project");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository("template-repository", templateRepository, true)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("project")
                        .template("template")
                        .repository("project-repository", projectRepository, true)
                        .repository("template-repository", templateRepository, false)
                        .plugin("module-path")
                        .conveyorJson(project)
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("module-path"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenRelativeRepositoryPath_whenConstructToStage_thenRepositoryPathResolvedRelativeToSchematicDefinitionDirectory(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path.resolve("repository"));

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(Paths.get("./temp/../repository"))
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenInheritedRelativeRepositoryPath_whenConstructToStage_thenRepositoryPathResolvedRelativeToDefiningSchematicDefinitionDirectory(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder()
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .install(path.resolve("repository"));
        var included = path.resolve("included");

        module.construct(
            factory.schematicDefinitionBuilder()
                .name("template")
                .repository(Paths.get("./repository"))
                .inclusion(
                    factory.schematicDefinitionBuilder()
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

        assertThat(defaultConstructionDirectory(included).resolve("instant")).exists();
    }

    @Test
    void givenRemoteRepository_whenConstructToStage_thenArtifactDownloadedFromUrl(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .jar(
                factory.jarBuilder("instant")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockServer);
        factory.repositoryBuilder()
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("local", path, true)
                .repository("remote", wireMockServer.baseUrl(), true)
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenRemoteRepository_whenConstructToStage_thenArtifactsAreCachedInDefaultDirectory(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .jar(
                factory.jarBuilder("instant")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockServer);
        factory.repositoryBuilder()
            .install(path);
        var schematic = factory.schematicDefinitionBuilder()
            .repository("local", path, true)
            .repository("remote", wireMockServer.baseUrl(), true)
            .plugin(
                "group",
                "instant",
                "1.0.0",
                Map.of("instant", "COMPILE-RUN")
            )
            .conveyorJson(path);

        module.construct(schematic, Stage.COMPILE);
        module.construct(schematic, Stage.COMPILE);

        assertThat(wireMockServer.getServeEvents().getRequests())
            .filteredOn(serveEvent -> serveEvent.getRequest().getUrl().contains("instant"))
            .hasSize(2);
        assertThat(defaultCacheDirectory(path))
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.jar")
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.json")
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.pom");
    }

    @Test
    void givenRemoteRepositoryCacheDirectoryProperty_whenConstructToStage_thenArtifactsAreCachedInSpecifiedDirectory(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .jar(
                factory.jarBuilder("instant")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockServer);
        factory.repositoryBuilder()
            .install(path);
        var cache = path.resolve("cache");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("local", path, true)
                .repository("remote", wireMockServer.baseUrl(), true)
                .property("conveyor.repository.remote.cache.directory", cache.toString())
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(cache)
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.jar")
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.json")
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.pom");
    }

    @Test
    void givenRemoteRepositoryCacheDirectoryProperty_whenConstructToStage_thenArtifactsAreCachedInSpecifiedDirectoryRelativeToSchematic(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .jar(
                factory.jarBuilder("instant")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockServer);
        factory.repositoryBuilder()
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("local", path, true)
                .repository("remote", wireMockServer.baseUrl(), true)
                .property("conveyor.repository.remote.cache.directory", "./temp/../cache")
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(path.resolve("cache"))
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.jar")
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.json")
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.pom");
    }

    @Test
    void givenSchematicDefinitionFromRemoteRepository_whenConstructToStage_thenTemplateIsTranslated(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .jar(
                factory.jarBuilder("module-path")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("module-path")
                    .parent("parent")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("parent")
                    .dependency("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependency")
            )
            .install(wireMockServer);
        factory.repositoryBuilder()
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("local", path, true)
                .repository("remote", wireMockServer.baseUrl(), true)
                .plugin("module-path")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenSchematicDefinitionFromRemoteRepository_whenConstructToStage_thenPreferencesAreTranslated(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .artifactId("bom")
                    .managedDependency("dependency", "1.0.0")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependency")
            )
            .install(wireMockServer);
        factory.repositoryBuilder()
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
                .repository("local", path, true)
                .repository("remote", wireMockServer.baseUrl(), true)
                .preferenceInclusion("bom")
                .plugin("dependencies")
                .dependency("dependency")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenSchematicDefinitionFromRemoteRepository_whenConstructToStage_thenPropertiesAreTranslated(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .artifactId("template")
                    .property("pom.key", "value")
            )
            .install(wireMockServer);
        factory.repositoryBuilder()
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
                .repository("local", path, true)
                .repository("remote", wireMockServer.baseUrl(), true)
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "pom.key")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .isEqualTo("pom.key=value");
    }

    @ParameterizedTest

    @Execution(ExecutionMode.SAME_THREAD)
    @CsvSource(
        textBlock = """
                    compile, IMPLEMENTATION
                    runtime, IMPLEMENTATION
                    test, TEST
                    system, IMPLEMENTATION
                    provided, IMPLEMENTATION
                    , IMPLEMENTATION
                    """
    )
    void givenSchematicDefinitionFromRemoteRepository_whenConstructToStage_thenScopeIsTranslated(
        String originalScope,
        DependencyScope dependencyScope,
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .artifactId("template")
                    .dependency("group", "dependency", "1.0.0", originalScope)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .install(wireMockServer);
        factory.repositoryBuilder()
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
                .repository("local", path, true)
                .repository("remote", wireMockServer.baseUrl(), true)
                .plugin(
                    "group",
                    "dependencies",
                    "1.0.0",
                    Map.of("scope", dependencyScope.toString())
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenNoGroupIdInPom_whenConstructToStage_thenGroupIsFromParent(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .artifactId("template")
                    .groupId(null)
                    .parent("group", "parent", "1.0.0")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("parent")
                    .groupId("group")
                    .version("1.0.0")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockServer);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("group", "template", "1.0.0")
                .repository("remote", wireMockServer.baseUrl(), true)
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenNoVersionInPom_whenConstructToStage_thenVersionIsFromParent(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .groupId("group")
                    .artifactId("template")
                    .version(null)
                    .parent("group", "parent", "2.0.0")
            )
            .pom(
                factory.pomBuilder()
                    .groupId("group")
                    .artifactId("parent")
                    .version("2.0.0")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockServer);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("group", "template", "2.0.0")
                .repository("remote", wireMockServer.baseUrl(), true)
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenImportScopedManagedDependency_whenConstructToStage_thenPreferencesIncludedFromThatDependency(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .artifactId("bom")
                    .managedDependency("instant", "1.0.0")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("template")
                    .managedDependency("bom", "1.0.0", "import")
            )
            .jar(
                factory.jarBuilder("instant")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockServer);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository("remote", wireMockServer.baseUrl(), true)
                .plugin(
                    "group",
                    "instant",
                    null,
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    void givenNoDependencyVersion_whenConstructToStage_thenVersionIsFromDependencyManagement(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .artifactId("template")
                    .managedDependency("dependency", "1.0.0")
                    .dependency("group", "dependency", null, null)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(wireMockServer);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository("remote", wireMockServer.baseUrl(), true)
                .plugin("dependencies")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenScopeFromDependencyManagement_whenConstructToStage_thenDependencyHasScopeFromDependencyManagement(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .artifactId("template")
                    .managedDependency("dependency", "1.0.0", "test")
                    .dependency("group", "dependency", null, null)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(wireMockServer);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository("remote", wireMockServer.baseUrl(), true)
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
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenScopeFromParentDependencyManagement_whenConstructToStage_thenDependencyHasScopeFromDependencyManagement(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .artifactId("parent")
                    .managedDependency("dependency", "1.0.0", "test")
            )
            .pom(
                factory.pomBuilder()
                    .parent("parent")
                    .artifactId("template")
                    .dependency("group", "dependency", null, null)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(wireMockServer);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository("remote", wireMockServer.baseUrl(), true)
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
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenProjectGroupIdProperty_whenConstructToStage_thenPropertyInterpolated(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .artifactId("template")
                    .dependency(
                        "${project.groupId}",
                        "dependency",
                        "1.0.0",
                        null
                    )
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(wireMockServer);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository("remote", wireMockServer.baseUrl(), true)
                .plugin("dependencies")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenProjectVersionProperty_whenConstructToStage_thenPropertyInterpolated(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .artifactId("template")
                    .dependency(
                        "group",
                        "dependency",
                        "${project.version}",
                        null
                    )
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependency")
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(wireMockServer);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository("remote", wireMockServer.baseUrl(), true)
                .plugin("dependencies")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenExcludedPomDependency_whenConstructToStage_thenDependencyIsExcluded(
        Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder()
            .pom(
                factory.pomBuilder()
                    .artifactId("dependency")
                    .dependency(
                        "group",
                        "transitive",
                        "1.0.0",
                        null,
                        new PomModel.Exclusion("group", "excluded")
                    )
            )
            .jar(
                factory.jarBuilder("dependency")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("transitive")
                    .dependency("excluded")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("transitive")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("excluded")
            )
            .jar(
                factory.jarBuilder("dependency")
                    .name("excluded")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies")
            )
            .install(wireMockServer);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(wireMockServer.baseUrl())
                .plugin("dependencies")
                .dependency("dependency")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("dependency-1.0.0", "transitive-1.0.0");
    }
}
