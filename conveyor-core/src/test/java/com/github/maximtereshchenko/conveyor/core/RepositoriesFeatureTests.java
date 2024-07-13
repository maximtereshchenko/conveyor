package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.Stage;
import com.github.maximtereshchenko.conveyor.api.schematic.DependencyScope;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.*;

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
                .repository("first", first)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("project")
                        .template("template")
                        .repository("second", second)
                        .plugin(
                            "group",
                            "instant",
                            "1.0.0",
                            Map.of("instant", "COMPILE-RUN")
                        )
                        .conveyorJson(project)
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(project.resolve("instant")).exists();
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
                .repository("main", templateRepository)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("project")
                        .template("template")
                        .repository("main", projectRepository)
                        .plugin(
                            "group",
                            "instant",
                            "1.0.0",
                            Map.of("instant", "COMPILE-RUN")
                        )
                        .conveyorJson(project)
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(project.resolve("instant")).exists();
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
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("instant")).exists();
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
            List.of(Stage.COMPILE)
        );

        assertThat(included.resolve("instant")).exists();
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

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("remote", wireMockServer.baseUrl())
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("instant")).exists();
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
        var schematic = factory.schematicDefinitionBuilder()
            .repository(wireMockServer.baseUrl())
            .plugin(
                "group",
                "instant",
                "1.0.0",
                Map.of("instant", "COMPILE-RUN")
            )
            .conveyorJson(path);

        module.construct(schematic, List.of(Stage.COMPILE));
        module.construct(schematic, List.of(Stage.COMPILE));

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
        var cache = path.resolve("cache");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(wireMockServer.baseUrl())
                .property("conveyor.repository.remote.cache.directory", cache.toString())
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
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

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(wireMockServer.baseUrl())
                .property("conveyor.repository.remote.cache.directory", "./temp/../cache")
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
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
                factory.jarBuilder("classpath", path)
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("classpath")
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

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(wireMockServer.baseUrl())
                .plugin("classpath")
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("classpath"))
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
                .preferenceInclusion("bom")
                .plugin("dependencies")
                .dependency("dependency")
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
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
                    Map.of("keys", "pom.key")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("properties"))
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
                    .dependency(
                        "group",
                        "dependency",
                        "1.0.0",
                        originalScope,
                        false,
                        List.of()
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
                .repository(wireMockServer.baseUrl())
                .plugin(
                    "group",
                    "dependencies",
                    "1.0.0",
                    Map.of("scope", dependencyScope.toString())
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
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
                .repository(wireMockServer.baseUrl())
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("instant")).exists();
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
                .repository(wireMockServer.baseUrl())
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("instant")).exists();
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
                .repository(wireMockServer.baseUrl())
                .plugin(
                    "group",
                    "instant",
                    null,
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("instant")).exists();
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
                    .dependency(
                        "group",
                        "dependency",
                        null,
                        null,
                        false,
                        List.of()
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
                .repository(wireMockServer.baseUrl())
                .plugin("dependencies")
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
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
                    .dependency(
                        "group",
                        "dependency",
                        null,
                        null,
                        false,
                        List.of()
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
                .repository(wireMockServer.baseUrl())
                .plugin(
                    "group",
                    "dependencies",
                    "1.0.0",
                    Map.of("scope", "TEST")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
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
                    .dependency(
                        "group",
                        "dependency",
                        null,
                        null,
                        false,
                        List.of()
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
                .repository(wireMockServer.baseUrl())
                .plugin(
                    "group",
                    "dependencies",
                    "1.0.0",
                    Map.of("scope", "TEST")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
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
                        null,
                        false,
                        List.of()
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
                .repository(wireMockServer.baseUrl())
                .plugin("dependencies")
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
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
                        null,
                        false,
                        List.of()
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
                .repository(wireMockServer.baseUrl())
                .plugin("dependencies")
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
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
                        false,
                        List.of(new PomModel.Exclusion("group", "excluded"))
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
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
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
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("properties"))
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

        assertThatCode(() -> module.construct(conveyorJson, List.of(Stage.COMPILE)))
            .doesNotThrowAnyException();
    }

    @Test
    void givenLocalDirectoryRepository_whenConstructToStage_thenArtifactIsPublished(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("artifact")
            )
            .jar(
                factory.jarBuilder("artifact", path)
            )
            .install(path);
        var published = path.resolve("published");

        module.construct(
            factory.schematicDefinitionBuilder()
                .group("schematic.group")
                .name("schematic-name")
                .version("1.2.3")
                .repository(path)
                .repository("published", published)
                .plugin(
                    "group",
                    "artifact",
                    "1.0.0",
                    Map.of(
                        "stage", "PUBLISH",
                        "repository", "published",
                        "path", Files.createFile(path.resolve("file")).toString()
                    )
                )
                .conveyorJson(path),
            List.of(Stage.PUBLISH)
        );

        assertThat(
            published.resolve("schematic")
                .resolve("group")
                .resolve("schematic-name")
                .resolve("1.2.3")
                .resolve("schematic-name-1.2.3.jar")
        )
            .exists();
    }

    @Test
    void givenNoArtifact_whenConstructToStage_thenExceptionIsThrown(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        var conveyorJson = factory.schematicDefinitionBuilder()
            .plugin("non-existent")
            .conveyorJson(path);
        var stages = List.of(Stage.COMPILE);

        assertThatThrownBy(() -> module.construct(conveyorJson, stages))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("group:non-existent:1.0.0");
    }

    @Test
    void givenOptionalPomDependency_whenConstructToStage_thenSchematicDefinitionDoesNotContainIt(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockServer wireMockServer
    ) throws Exception {
        factory.repositoryBuilder(path)
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
                    .dependency(
                        "group",
                        "optional",
                        "1.0.0",
                        null,
                        true,
                        List.of()
                    )
            )
            .jar(
                factory.jarBuilder("instant", path)
            )
            .install(wireMockServer);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(wireMockServer.baseUrl())
                .plugin(
                    "group",
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("instant")).exists();
    }
}
