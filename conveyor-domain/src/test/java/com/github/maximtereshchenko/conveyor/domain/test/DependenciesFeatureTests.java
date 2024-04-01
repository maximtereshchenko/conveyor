package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class DependenciesFeatureTests extends ConveyorTest {

    @Test
    void givenTemplateHasDifferentDependency_whenConstructToStage_thenSchematicHasBothDependencies(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .dependency("template-dependency", 1, DependencyScope.IMPLEMENTATION)
            )
            .manual(builder -> builder.name("template-dependency").version(1))
            .jar("dependency", builder -> builder.name("template-dependency").version(1))
            .manual(builder -> builder.name("schematic-dependency").version(1))
            .jar("dependency", builder -> builder.name("schematic-dependency").version(1))
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .template("template", 1)
                .plugin("dependencies", 1, Map.of())
                .dependency("schematic-dependency", 1, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(2)
            .contains("template-dependency-1", "schematic-dependency-1");
    }

    @Test
    void givenSchematicOverridesDependency_whenConstructToStage_thenVersionIsOverridden(
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
            .manual(builder -> builder.name("dependency").version(2))
            .jar("dependency", builder -> builder.name("dependency").version(2))
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .template("template", 1)
                .plugin("dependencies", 1, Map.of())
                .dependency("dependency", 2, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(1)
            .contains("dependency-2");
    }

    @Test
    void givenSchematicOverridesDependency_whenConstructToStage_thenScopeIsOverridden(
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
                .repository(path)
                .template("template", 1)
                .plugin("dependencies", 1, Map.of())
                .dependency("dependency", 1, DependencyScope.IMPLEMENTATION)
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("dependencies"))
            .content()
            .hasLineCount(1)
            .contains("dependency-1");
    }

    @Test
    void givenDependencyOnOtherSchematic_whenConstructToStage_thenModuleProductIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("product").version(1))
            .jar("product", builder -> builder.name("product").version(1))
            .jar("dependency", builder -> builder.name("first").version(1))
            .manual(builder -> builder.name("dependencies").version(1))
            .jar("dependencies", builder -> builder.name("dependencies").version(1))
            .install(path);
        var second = path.resolve("second");

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .inclusion(
                    factory.schematicBuilder()
                        .name("first")
                        .version(1)
                        .plugin(
                            "product",
                            1,
                            Map.of("path", path.resolve("first-1.jar").toString())
                        )
                        .install(path.resolve("first"))
                )
                .inclusion(
                    factory.schematicBuilder()
                        .name("second")
                        .version(1)
                        .plugin("dependencies", 1, Map.of())
                        .schematicDependency("first", DependencyScope.IMPLEMENTATION)
                        .install(second)
                )
                .install(path),
            Stage.ARCHIVE
        );

        assertThat(defaultConstructionDirectory(second).resolve("dependencies"))
            .content()
            .isEqualTo("first-1");
    }
}
