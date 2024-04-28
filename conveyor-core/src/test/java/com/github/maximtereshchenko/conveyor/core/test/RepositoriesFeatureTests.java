package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(WireMockExtension.class)
final class RepositoriesFeatureTests extends ConveyorTest {

    @Test
    void givenSchematicTemplateHasDifferentRepository_whenConstructToStage_thenSchematicHasBothRepositories(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        var first = path.resolve("first");
        var second = path.resolve("second");
        factory.repositoryBuilder(path)
            .install(first);
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant", path)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        var templateRepository = path.resolve("template-repository");
        factory.repositoryBuilder(path)
            .install(templateRepository);
        var projectRepository = path.resolve("project-repository");
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant", path)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        var templateRepository = path.resolve("template-repository");
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("class-path")
            )
            .jar(
                factory.jarBuilder("class-path", path)
            )
            .install(templateRepository);
        var projectRepository = path.resolve("project-repository");
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("class-path")
                    .dependency("dependency")
            )
            .jar(
                factory.jarBuilder("class-path", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
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
                        .plugin("class-path")
                        .conveyorJson(project)
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("class-path"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenRelativeRepositoryPath_whenConstructToStage_thenRepositoryPathResolvedRelativeToSchematicDefinitionDirectory(
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
            .jar(
                factory.jarBuilder("instant", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockServer);
        factory.repositoryBuilder(path)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
            .jar(
                factory.jarBuilder("instant", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockServer);
        factory.repositoryBuilder(path)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
            .jar(
                factory.jarBuilder("instant", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockServer);
        factory.repositoryBuilder(path)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
            .jar(
                factory.jarBuilder("instant", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockServer);
        factory.repositoryBuilder(path)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
            .jar(
                factory.jarBuilder("class-path", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("class-path")
                    .parent("parent")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("parent")
                    .dependency("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependency")
            )
            .install(wireMockServer);
        factory.repositoryBuilder(path)
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("local", path, true)
                .repository("remote", wireMockServer.baseUrl(), true)
                .plugin("class-path")
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("class-path"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenSchematicDefinitionFromRemoteRepository_whenConstructToStage_thenPreferencesAreTranslated(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
            .pom(
                factory.pomBuilder()
                    .artifactId("bom")
                    .managedDependency("dependency", "1.0.0")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependency")
            )
            .install(wireMockServer);
        factory.repositoryBuilder(path)
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
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenSchematicDefinitionFromRemoteRepository_whenConstructToStage_thenPropertiesAreTranslated(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
            .pom(
                factory.pomBuilder()
                    .artifactId("template")
                    .property("pom.key", "value")
            )
            .install(wireMockServer);
        factory.repositoryBuilder(path)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("dependency", path)
            )
            .install(wireMockServer);
        factory.repositoryBuilder(path)
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
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenNoGroupIdInPom_whenConstructToStage_thenGroupIsFromParent(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("instant", path)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("instant", path)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("instant", path)
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
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("dependency", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
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
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenScopeFromDependencyManagement_whenConstructToStage_thenDependencyHasScopeFromDependencyManagement(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("dependency", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
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
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenScopeFromParentDependencyManagement_whenConstructToStage_thenDependencyHasScopeFromDependencyManagement(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("dependency", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
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
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenProjectGroupIdProperty_whenConstructToStage_thenPropertyInterpolated(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("dependency", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
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
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenProjectVersionProperty_whenConstructToStage_thenPropertyInterpolated(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("dependency", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
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
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenExcludedPomDependency_whenConstructToStage_thenDependencyIsExcluded(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("dependency", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("transitive")
                    .dependency("excluded")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("transitive")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("excluded")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("excluded")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
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
            .contains("group-dependency-1.0.0", "transitive-1.0.0");
    }

    @Test
    void givenDuplicatePomProperty_whenConstructToStage_thenPropertyValueOverridden(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
            .pom(
                factory.pomBuilder()
                    .artifactId("template")
                    .property("duplicate.key", "initial")
                    .property("duplicate.key", "overridden")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("properties")
            )
            .jar(
                factory.jarBuilder("properties", path)
            )
            .install(wireMockServer);

        module.construct(
            factory.schematicDefinitionBuilder()
                .template("template")
                .repository(wireMockServer.baseUrl())
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "duplicate.key")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .isEqualTo("duplicate.key=overridden");
    }

    @Test
    void givenNoPropertyToInterpolateInImportedManagedDependency_whenConstructToStage_thenSkipIt(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
                    .managedDependency("without-version", "${no.such.property}")
            )
            .jar(
                factory.jarBuilder("instant", path)
            )
            .install(wireMockServer);
        var conveyorJson = factory.schematicDefinitionBuilder()
            .repository(wireMockServer.baseUrl())
            .plugin("instant")
            .conveyorJson(path);

        assertThatCode(() -> module.construct(conveyorJson, Stage.COMPILE))
            .doesNotThrowAnyException();
    }
}
