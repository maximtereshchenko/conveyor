package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class PropertiesFeatureTests extends ConveyorTest {

    @Test
    void givenTemplateHasDifferentProperty_whenConstructToStage_thenSchematicHasBothProperties(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .property("template.key", "template.value")
            )
            .manual(builder -> builder.name("properties").version(1))
            .jar("properties", builder -> builder.name("properties").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .template("template", 1)
                .property("key", "value")
                .plugin("properties", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(5)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path),
                "template.key=template.value",
                "key=value"
            );
    }

    @Test
    void givenSchematicHasPropertyWithSameKey_whenConstructToStage_thenSchematicPropertyValueIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .property("key", "template.value")
            )
            .manual(builder -> builder.name("properties").version(1))
            .jar("properties", builder -> builder.name("properties").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .template("template", 1)
                .property("key", "value")
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
                "key=value"
            );
    }

    @Test
    void givenSchematicAssignedEmptyValueToInheritedProperty_whenConstructToStage_thenPropertyWasRemoved(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder ->
                builder.name("template")
                    .version(1)
                    .property("to.be.removed", "value")
            )
            .manual(builder -> builder.name("properties").version(1))
            .jar("properties", builder -> builder.name("properties").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .template("template", 1)
                .property("to.be.removed", "")
                .plugin("properties", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(3)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path)
            );
    }

    @Test
    void givenSchematicNamePropertyIsOverridden_whenConstructToStage_thenPropertyWasNotChanged(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("properties").version(1))
            .jar("properties", builder -> builder.name("properties").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .property("conveyor.schematic.name", "custom")
                .plugin("properties", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(3)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path)
            );
    }

    @Test
    void givenDiscoveryDirectoryProperty_whenConstructToStage_thenPluginsWorkInThisDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("construction-directory").version(1))
            .jar("construction-directory", builder -> builder.name("construction-directory").version(1))
            .install(path);
        var project = path.resolve("project");

        var projectBuildFiles = module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .property("conveyor.discovery.directory", project.toString())
                .plugin("construction-directory", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", ProductType.MODULE))
            .containsExactly(defaultConstructionDirectory(project));
    }

    @Test
    void givenRelativeDiscoveryDirectoryProperty_whenConstructToStage_thenDiscoveryDirectoryResolvedRelativeToSchematicDefinitionDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("construction-directory").version(1))
            .jar("construction-directory", builder -> builder.name("construction-directory").version(1))
            .install(path);

        var projectBuildFiles = module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .property("conveyor.discovery.directory", "./temp/../project")
                .plugin("construction-directory", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", ProductType.MODULE))
            .containsExactly(defaultConstructionDirectory(path.resolve("project")));
    }

    @Test
    void givenConstructionDirectoryProperty_whenConstructToStage_thenPluginsPlacedProductsInThisDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("construction-directory").version(1))
            .jar("construction-directory", builder -> builder.name("construction-directory").version(1))
            .install(path);
        var construction = path.resolve("construction");

        var projectBuildFiles = module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .property("conveyor.construction.directory", construction.toString())
                .plugin("construction-directory", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", ProductType.MODULE))
            .containsExactly(construction);
    }

    @Test
    void givenRelativeConstructionDirectoryProperty_whenConstructToStage_thenConstructionDirectoryResolvedRelativeToDiscoveryDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("construction-directory").version(1))
            .jar("construction-directory", builder -> builder.name("construction-directory").version(1))
            .install(path);

        var projectBuildFiles = module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .property("conveyor.construction.directory", "./temp/../construction")
                .plugin("construction-directory", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(projectBuildFiles.byType("project", ProductType.MODULE))
            .containsExactly(path.resolve("construction"));
    }

    @Test
    void givenPropertyIsTemplatedWithOtherProperty_whenConstructToStage_thenPropertyIsInterpolated(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("properties").version(1))
            .jar("properties", builder -> builder.name("properties").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .property("key", "interpolated")
                .property("templated", "${key}-suffix")
                .plugin("properties", 1, Map.of())
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(5)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path),
                "key=interpolated",
                "templated=interpolated-suffix"
            );
    }

    @Test
    void givenConfigurationTemplatedWithOtherTemplatedProperty_whenConstructToStage_thenConfigurationInterpolatedWithInterpolatedProperty(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) {
        factory.repositoryBuilder()
            .superManual()
            .manual(builder -> builder.name("configuration").version(1))
            .jar("configuration", builder -> builder.name("configuration").version(1))
            .install(path);

        module.construct(
            factory.schematicBuilder()
                .name("project")
                .version(1)
                .repository(path)
                .property("key", "interpolated")
                .property("templated", "${key}-suffix")
                .plugin("configuration", 1, Map.of("key", "prefix-${templated}"))
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("configuration"))
            .content()
            .hasLineCount(2)
            .contains(
                "enabled=true",
                "key=prefix-interpolated-suffix"
            );
    }
}
