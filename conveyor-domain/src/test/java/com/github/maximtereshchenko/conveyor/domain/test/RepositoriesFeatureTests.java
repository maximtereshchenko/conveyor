package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.wiremock.junit5.WireMockExtension;
import com.github.maximtereshchenko.conveyor.wiremock.junit5.WireMockRuntimeInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class RepositoriesFeatureTests extends ConveyorTest {

    @Test
    void givenSchematicTemplateHasDifferentRepository_whenConstructToStage_thenSchematicHasBothRepositories(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
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
                        .repository("second", second, true)
                        .plugin(
                            "instant",
                            "1.0.0",
                            Map.of("instant", "COMPILE-RUN")
                        )
                        .install(project)
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("instant")).exists();
    }

    @Test
    void givenSchematicTemplateHasSameRepository_whenConstructToStage_thenRepositoryPathIsOverridden(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
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
                        .repository("main", projectRepository, true)
                        .plugin(
                            "instant",
                            "1.0.0",
                            Map.of("instant", "COMPILE-RUN")
                        )
                        .install(project)
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("instant")).exists();
    }

    @Test
    void givenSchematicTemplateHasSameRepository_whenConstructToStage_thenRepositoryEnabledFlagIsOverridden(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
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
                        .repository("project-repository", projectRepository, true)
                        .repository("template-repository", templateRepository, false)
                        .plugin("module-path")
                        .install(project)
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(project).resolve("module-path"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    void givenRelativeRepositoryPath_whenConstructToStage_thenRepositoryPathResolvedRelativeToDiscoveryDirectory(
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
            .install(path.resolve("repository"));

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(Paths.get("./temp/../repository"))
                .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    @ExtendWith(WireMockExtension.class)
    void givenRemoteRepository_whenConstructToStage_thenArtifactDownloadedFromUrl(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockRuntimeInfo wireMockRuntimeInfo
    ) {
        factory.repositoryBuilder()
            .jar(
                factory.jarBuilder("instant")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockRuntimeInfo.getWireMock());
        factory.repositoryBuilder()
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("local", path, true)
                .repository("remote", wireMockRuntimeInfo.getHttpBaseUrl(), true)
                .plugin(
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }

    @Test
    @ExtendWith(WireMockExtension.class)
    void givenRemoteRepository_whenConstructToStage_thenArtifactsAreCachedInDefaultDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockRuntimeInfo wireMockRuntimeInfo
    ) {
        factory.repositoryBuilder()
            .jar(
                factory.jarBuilder("instant")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockRuntimeInfo.getWireMock());
        factory.repositoryBuilder()
            .install(path);
        var schematic = factory.schematicDefinitionBuilder()
            .repository("local", path, true)
            .repository("remote", wireMockRuntimeInfo.getHttpBaseUrl(), true)
            .plugin(
                "instant",
                "1.0.0",
                Map.of("instant", "COMPILE-RUN")
            )
            .install(path);

        module.construct(schematic, Stage.COMPILE);
        module.construct(schematic, Stage.COMPILE);

        assertThat(wireMockRuntimeInfo.getWireMock().getServeEvents())
            .filteredOn(serveEvent -> serveEvent.getRequest().getUrl().contains("instant"))
            .hasSize(2);
        assertThat(defaultCacheDirectory(path))
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.{json,jar}");
    }

    @Test
    @ExtendWith(WireMockExtension.class)
    void givenRemoteRepositoryCacheDirectoryProperty_whenConstructToStage_thenArtifactsAreCachedInSpecifiedDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockRuntimeInfo wireMockRuntimeInfo
    ) {
        factory.repositoryBuilder()
            .jar(
                factory.jarBuilder("instant")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockRuntimeInfo.getWireMock());
        factory.repositoryBuilder()
            .install(path);
        var cache = path.resolve("cache");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("local", path, true)
                .repository("remote", wireMockRuntimeInfo.getHttpBaseUrl(), true)
                .property("conveyor.repository.remote.cache.directory", cache.toString())
                .plugin(
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(cache)
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.{json,jar}");
    }

    @Test
    @ExtendWith(WireMockExtension.class)
    void givenRemoteRepositoryCacheDirectoryProperty_whenConstructToStage_thenArtifactsAreCachedInSpecifiedDirectoryRelativeToSchematic(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockRuntimeInfo wireMockRuntimeInfo
    ) {
        factory.repositoryBuilder()
            .jar(
                factory.jarBuilder("instant")
            )
            .pom(
                factory.pomBuilder()
                    .artifactId("instant")
            )
            .install(wireMockRuntimeInfo.getWireMock());
        factory.repositoryBuilder()
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("local", path, true)
                .repository("remote", wireMockRuntimeInfo.getHttpBaseUrl(), true)
                .property("conveyor.repository.remote.cache.directory", "./temp/../cache")
                .plugin(
                    "instant",
                    "1.0.0",
                    Map.of("instant", "COMPILE-RUN")
                )
                .install(path),
            Stage.COMPILE
        );

        assertThat(path.resolve("cache"))
            .isDirectoryRecursivelyContaining("glob:**instant-1.0.0.{json,jar}");
    }

    @Test
    @ExtendWith(WireMockExtension.class)
    void givenManualDefinitionFromRemoteRepository_whenConstructToStage_thenTemplateIsTranslated(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockRuntimeInfo wireMockRuntimeInfo
    ) {
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
            .install(wireMockRuntimeInfo.getWireMock());
        factory.repositoryBuilder()
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("local", path, true)
                .repository("remote", wireMockRuntimeInfo.getHttpBaseUrl(), true)
                .plugin("module-path")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("module-path"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }

    @Test
    @ExtendWith(WireMockExtension.class)
    void givenManualDefinitionFromRemoteRepository_whenConstructToStage_thenPreferencesAreTranslated(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory,
        WireMockRuntimeInfo wireMockRuntimeInfo
    ) {
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
            .install(wireMockRuntimeInfo.getWireMock());
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
                .repository("remote", wireMockRuntimeInfo.getHttpBaseUrl(), true)
                .preferenceInclusion("bom", "1.0.0")
                .plugin("dependencies")
                .dependency("dependency")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .isEqualTo("dependency-1.0.0");
    }
}
