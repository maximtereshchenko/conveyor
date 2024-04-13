package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class PropertiesFeatureTests extends ConveyorTest {

    @Test
    void givenTemplateHasDifferentProperty_whenConstructToStage_thenSchematicHasBothProperties(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .plugin(
                    "properties",
                    "1.0.0",
                    Map.of("keys", "template.key,key")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(2)
            .contains("template.key=template.value", "key=value");
    }

    @Test
    void givenSchematicHasPropertyWithSameKey_whenConstructToStage_thenSchematicPropertyValueIsUsed(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .plugin("properties", "1.0.0", Map.of("keys", "key"))
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .isEqualTo("key=value");
    }

    @Test
    void givenSchematicAssignedEmptyValueToInheritedProperty_whenConstructToStage_thenPropertyWasRemoved(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .plugin("properties", "1.0.0", Map.of("keys", "to.be.removed"))
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .isEqualTo("to.be.removed=");
    }

    @Test
    void givenSchematicNamePropertyIsOverridden_whenConstructToStage_thenPropertyWasNotChanged(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .plugin(
                    "properties",
                    "1.0.0",
                    Map.of("keys", "conveyor.schematic.name")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .isEqualTo("conveyor.schematic.name=project");
    }

    @Test
    void givenDiscoveryDirectoryProperty_whenConstructToStage_thenPluginsWorkInThisDirectory(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .plugin(
                    "properties",
                    "1.0.0",
                    Map.of("keys", "conveyor.discovery.directory")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .isEqualTo("conveyor.discovery.directory=" + project);
    }

    @Test
    void givenRelativeDiscoveryDirectoryProperty_whenConstructToStage_thenDiscoveryDirectoryResolvedRelativeToSchematicDefinitionDirectory(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .plugin(
                    "properties",
                    "1.0.0",
                    Map.of("keys", "conveyor.discovery.directory")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .isEqualTo("conveyor.discovery.directory=" + path.resolve("project"));
    }

    @Test
    void givenConstructionDirectoryProperty_whenConstructToStage_thenPluginsPlacedProductsInThisDirectory(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .plugin(
                    "properties",
                    "1.0.0",
                    Map.of("keys", "conveyor.construction.directory")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(construction.resolve("properties"))
            .content()
            .isEqualTo("conveyor.construction.directory=" + construction);
    }

    @Test
    void givenRelativeConstructionDirectoryProperty_whenConstructToStage_thenConstructionDirectoryResolvedRelativeToSchematicDefinitionDirectory(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .plugin(
                    "properties",
                    "1.0.0",
                    Map.of("keys", "conveyor.construction.directory")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        var construction = path.resolve("construction");
        assertThat(construction.resolve("properties"))
            .content()
            .isEqualTo("conveyor.construction.directory=" + construction);
    }

    @Test
    void givenPropertyIsTemplatedWithOtherProperty_whenConstructToStage_thenPropertyIsInterpolated(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .plugin(
                    "properties",
                    "1.0.0",
                    Map.of("keys", "templated,key")
                )
                .conveyorJson(path),
            Stage.COMPILE
        );

        assertThat(defaultConstructionDirectory(path).resolve("properties"))
            .content()
            .hasLineCount(2)
            .contains("key=interpolated", "templated=interpolated-suffix");
    }

    @Test
    void givenConfigurationTemplatedWithOtherTemplatedProperty_whenConstructToStage_thenConfigurationInterpolatedWithInterpolatedProperty(
        Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .conveyorJson(path),
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
