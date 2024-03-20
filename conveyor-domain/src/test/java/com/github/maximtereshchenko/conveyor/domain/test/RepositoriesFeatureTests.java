package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
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
            .superManual()
            .install(first);
        factory.repositoryBuilder()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .install(second);
        var project = path.resolve("project");

        module.construct(
            factory.schematicBuilder()
                .name("template")
                .version("1.0.0")
                .repository("first", first, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("project")
                        .version("1.0.0")
                        .repository("second", second, true)
                        .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
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
            .superManual()
            .install(templateRepository);
        var projectRepository = path.resolve("project-repository");
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .install(projectRepository);
        var project = path.resolve("project");

        module.construct(
            factory.schematicBuilder()
                .name("template")
                .version("1.0.0")
                .repository("main", templateRepository, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("project")
                        .version("1.0.0")
                        .repository("main", projectRepository, true)
                        .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
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
            .superManual()
            .manual(builder -> builder.name("module-path").version("1.0.0"))
            .jar("module-path", builder -> builder.name("module-path").version("1.0.0"))
            .install(templateRepository);
        var projectRepository = path.resolve("project-repository");
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("module-path")
                    .version("1.0.0")
                    .dependency("dependency", "1.0.0", DependencyScope.IMPLEMENTATION)
            )
            .jar("module-path", builder -> builder.name("module-path").version("1.0.0"))
            .manual(builder -> builder.name("dependency").version("1.0.0"))
            .jar("dependency", builder -> builder.name("dependency").version("1.0.0"))
            .install(projectRepository);
        var project = path.resolve("project");

        module.construct(
            factory.schematicBuilder()
                .name("template")
                .version("1.0.0")
                .repository("template-repository", templateRepository, true)
                .inclusion(
                    factory.schematicBuilder()
                        .name("project")
                        .version("1.0.0")
                        .repository("project-repository", projectRepository, true)
                        .repository("template-repository", templateRepository, false)
                        .plugin("module-path", "1.0.0", Map.of())
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
            .superManual()
            .manual(builder -> builder.name("instant").version("1.0.0"))
            .jar("instant", builder -> builder.name("instant").version("1.0.0"))
            .install(path.resolve("repository"));

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version("1.0.0")
                .repository("main", Paths.get("./temp/../repository"), true)
                .plugin("instant", "1.0.0", Map.of("instant", "COMPILE-RUN"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("instant")).exists();
    }
}
