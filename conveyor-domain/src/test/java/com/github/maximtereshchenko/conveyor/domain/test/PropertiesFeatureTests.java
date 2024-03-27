package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
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
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .property("template.key", "template.value")
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
                .repository("main", path, true)
                .template("template")
                .property("key", "value")
                .plugin("properties")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(7)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.schematic.version=1",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path),
                "conveyor.repository.remote.cache.directory=" + defaultCacheDirectory(path),
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
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .property("key", "template.value")
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
                .repository("main", path, true)
                .template("template")
                .property("key", "value")
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
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .property("to.be.removed", "value")
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
                .repository("main", path, true)
                .template("template")
                .property("to.be.removed", "")
                .plugin("properties")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(5)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.schematic.version=1",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path),
                "conveyor.repository.remote.cache.directory=" + defaultCacheDirectory(path)
            );
    }

    @Test
    void givenSchematicNamePropertyIsOverridden_whenConstructToStage_thenPropertyWasNotChanged(
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

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("main", path, true)
                .property("conveyor.schematic.name", "custom")
                .plugin("properties")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(5)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.schematic.version=1",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path),
                "conveyor.repository.remote.cache.directory=" + defaultCacheDirectory(path)
            );
    }

    @Test
    void givenDiscoveryDirectoryProperty_whenConstructToStage_thenPluginsWorkInThisDirectory(
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

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("main", path, true)
                .property("conveyor.discovery.directory", project.toString())
                .plugin("properties")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(5)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.schematic.version=1",
                "conveyor.discovery.directory=" + project,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path),
                "conveyor.repository.remote.cache.directory=" + defaultCacheDirectory(path)
            );
    }

    @Test
    void givenRelativeDiscoveryDirectoryProperty_whenConstructToStage_thenDiscoveryDirectoryResolvedRelativeToSchematicDefinitionDirectory(
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

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("main", path, true)
                .property("conveyor.discovery.directory", "./temp/../project")
                .plugin("properties")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(5)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.schematic.version=1",
                "conveyor.discovery.directory=" + path.resolve("project"),
                "conveyor.construction.directory=" + defaultConstructionDirectory(path),
                "conveyor.repository.remote.cache.directory=" + defaultCacheDirectory(path)
            );
    }

    @Test
    void givenConstructionDirectoryProperty_whenConstructToStage_thenPluginsPlacedProductsInThisDirectory(
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
        var construction = path.resolve("construction");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("main", path, true)
                .property("conveyor.construction.directory", construction.toString())
                .plugin("properties")
                .install(path),
            Stage.COMPILE
        );

        assertThat(construction.resolve("properties"))
            .content()
            .hasLineCount(5)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.schematic.version=1",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + construction,
                "conveyor.repository.remote.cache.directory=" + defaultCacheDirectory(path)
            );
    }

    @Test
    void givenRelativeConstructionDirectoryProperty_whenConstructToStage_thenConstructionDirectoryResolvedRelativeToSchematicDefinitionDirectory(
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

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("main", path, true)
                .property("conveyor.construction.directory", "./temp/../construction")
                .plugin("properties")
                .install(path),
            Stage.COMPILE
        );

        var construction = path.resolve("construction");
        assertThat(construction.resolve("properties"))
            .content()
            .hasLineCount(5)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.schematic.version=1",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + construction,
                "conveyor.repository.remote.cache.directory=" + defaultCacheDirectory(path)
            );
    }

    @Test
    void givenPropertyIsTemplatedWithOtherProperty_whenConstructToStage_thenPropertyIsInterpolated(
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

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("main", path, true)
                .property("key", "interpolated")
                .property("templated", "${key}-suffix")
                .plugin("properties")
                .install(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(7)
            .contains(
                "conveyor.schematic.name=project",
                "conveyor.schematic.version=1",
                "conveyor.discovery.directory=" + path,
                "conveyor.construction.directory=" + defaultConstructionDirectory(path),
                "conveyor.repository.remote.cache.directory=" + defaultCacheDirectory(path),
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
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("configuration")
            )
            .jar(
                factory.jarBuilder("configuration")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository("main", path, true)
                .property("key", "interpolated")
                .property("templated", "${key}-suffix")
                .plugin(
                    "configuration",
                    "1.0.0",
                    Map.of("key", "prefix-${templated}")
                )
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
