package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.Stage;
import com.github.maximtereshchenko.conveyor.api.schematic.DependencyScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class PropertiesFeatureTests extends ConveyorTest {

    @Test
    void givenTemplateHasDifferentProperty_whenConstructToStage_thenSchematicHasBothProperties(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("properties", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .property("key", "value")
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "template.key,key")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("properties"))
            .content()
            .hasLineCount(2)
            .contains("template.key=template.value", "key=value");
    }

    @Test
    void givenSchematicHasPropertyWithSameKey_whenConstructToStage_thenSchematicPropertyValueIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("properties", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .property("key", "value")
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "key")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("properties"))
            .content()
            .isEqualTo("key=value");
    }

    @Test
    void givenSchematicAssignedEmptyValueToInheritedProperty_whenConstructToStage_thenPropertyWasRemoved(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
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
                factory.jarBuilder("properties", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .template("template")
                .property("to.be.removed", "")
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "to.be.removed")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("properties"))
            .content()
            .isEqualTo("to.be.removed=");
    }

    @Test
    void givenSchematicNamePropertyIsOverridden_whenConstructToStage_thenPropertyWasNotChanged(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .repository(path)
                .property("conveyor.schematic.name", "custom")
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "conveyor.schematic.name")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("properties"))
            .content()
            .isEqualTo("conveyor.schematic.name=project");
    }

    @Test
    void givenSchematicDoesNotHaveGroup_whenConstructToStage_thenGroupIsEqualToTemplateGroup(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("properties")
            )
            .jar(
                factory.jarBuilder("properties", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .group("template.group")
                    .name("template")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .group(null)
                .template("template.group", "template", "1.0.0")
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "conveyor.schematic.group")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("properties"))
            .content()
            .isEqualTo("conveyor.schematic.group=template.group");
    }

    @Test
    void givenSchematicDoesNotHaveVersion_whenConstructToStage_thenVersionIsEqualToTemplateVersion(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("properties")
            )
            .jar(
                factory.jarBuilder("properties", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("template")
                    .version("2.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .version(null)
                .template("group", "template", "2.0.0")
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "conveyor.schematic.version")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("properties"))
            .content()
            .isEqualTo("conveyor.schematic.version=2.0.0");
    }

    @Test
    void givenSchematicGroupPropertyIsOverridden_whenConstructToStage_thenPropertyWasNotChanged(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .repository(path)
                .property("conveyor.schematic.group", "custom")
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "conveyor.schematic.group")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("properties"))
            .content()
            .isEqualTo("conveyor.schematic.group=group");
    }

    @Test
    void givenSchematicDirectoryProperty_whenConstructToStage_thenValueIsEqualToActualDirectory(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("properties")
            )
            .jar(
                factory.jarBuilder("properties", path)
            )
            .install(path);
        var conveyorJson = factory.schematicDefinitionBuilder()
            .repository(path)
            .plugin(
                "group",
                "properties",
                "1.0.0",
                Map.of("keys", "conveyor.schematic.directory")
            )
            .conveyorJson(path);

        module.construct(conveyorJson, List.of(Stage.COMPILE));

        assertThat(path.resolve("properties"))
            .content()
            .isEqualTo("conveyor.schematic.directory=" + conveyorJson.getParent());
    }

    @Test
    void givenSchematicDirectoryPropertyIsOverridden_whenConstructToStage_thenPropertyWasNotChanged(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("properties")
            )
            .jar(
                factory.jarBuilder("properties", path)
            )
            .install(path);
        var conveyorJson = factory.schematicDefinitionBuilder()
            .repository(path)
            .property("conveyor.schematic.directory", "custom")
            .plugin(
                "group",
                "properties",
                "1.0.0",
                Map.of("keys", "conveyor.schematic.directory")
            )
            .conveyorJson(path);

        module.construct(conveyorJson, List.of(Stage.COMPILE));

        assertThat(path.resolve("properties"))
            .content()
            .isEqualTo("conveyor.schematic.directory=" + conveyorJson.getParent());
    }

    @Test
    void givenPropertyIsTemplatedWithOtherProperty_whenConstructToStage_thenPropertyIsInterpolated(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
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
                .repository(path)
                .property("key", "interpolated")
                .property("templated", "${key}-suffix")
                .plugin(
                    "group",
                    "properties",
                    "1.0.0",
                    Map.of("keys", "templated,key")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("properties"))
            .content()
            .hasLineCount(2)
            .contains("key=interpolated", "templated=interpolated-suffix");
    }

    @Test
    void givenConfigurationTemplatedWithOtherTemplatedProperty_whenConstructToStage_thenConfigurationInterpolatedWithInterpolatedProperty(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("configuration")
            )
            .jar(
                factory.jarBuilder("configuration", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("key", "interpolated")
                .property("templated", "${key}-suffix")
                .plugin(
                    "group",
                    "configuration",
                    "1.0.0",
                    Map.of("key", "prefix-${templated}")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("configuration"))
            .content()
            .hasLineCount(2)
            .contains(
                "enabled=true",
                "key=prefix-interpolated-suffix"
            );
    }

    @Test
    void givenInterpolatedArtifactDependencyVersion_whenConstructToStage_thenDependencyWithVersionFromPropertyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("dependency.version", "1.0.0")
                .plugin("dependencies")
                .dependency(
                    "group",
                    "dependency",
                    "${dependency.version}",
                    DependencyScope.IMPLEMENTATION
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .isEqualTo("group-dependency-1.0.0");
    }

    @Test
    void givenInterpolatedArtifactDependencyVersionInSchematicDependency_whenConstructToStage_thenDependencyWithVersionFromOtherSchematicPropertyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("artifact")
            )
            .jar(
                factory.jarBuilder("artifact", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("library")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .name("library")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .install(path);
        var depends = path.resolve("depends");

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("dependency")
                        .template("project")
                        .property("library.version", "1.0.0")
                        .plugin(
                            "group",
                            "artifact",
                            "1.0.0",
                            Map.of(
                                "path",
                                path.resolve("group")
                                    .resolve("dependency")
                                    .resolve("1.0.0")
                                    .resolve("dependency-1.0.0.jar")
                                    .toString()
                            )
                        )
                        .dependency(
                            "group",
                            "library",
                            "${library.version}",
                            DependencyScope.IMPLEMENTATION
                        )
                        .conveyorJson(path.resolve("dependency"))
                )
                .inclusion(
                    factory.schematicDefinitionBuilder()
                        .name("depends")
                        .template("project")
                        .plugin(
                            "group",
                            "dependencies",
                            "1.0.0",
                            Map.of()
                        )
                        .dependency("dependency")
                        .conveyorJson(depends)
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(depends.resolve("dependencies"))
            .content(StandardCharsets.UTF_8)
            .hasLineCount(2)
            .contains("group-dependency-1.0.0", "library-1.0.0");
    }

    @Test
    void givenInterpolatedPluginVersion_whenConstructToStage_thenPluginWithVersionFromPropertyIsUsed(
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
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("instant.version", "1.0.0")
                .plugin(
                    "group",
                    "instant",
                    "${instant.version}",
                    Map.of("instant", "COMPILE-RUN")
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("instant")).exists();
    }

    @Test
    void givenInterpolatedArtifactPreferenceVersion_whenConstructToStage_thenPluginWithVersionFromPropertyIsUsed(
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
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("instant.version", "1.0.0")
                .preference("instant", "${instant.version}")
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
    void givenInterpolatedPreferenceInclusionVersion_whenConstructToStage_thenPluginWithVersionFromPropertyIsUsed(
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
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("bom")
                    .preference("instant", "1.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("bom.version", "1.0.0")
                .preferenceInclusion("group", "bom", "${bom.version}")
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
    void givenInterpolatedPluginGroup_whenConstructToStage_thenPluginWithGroupFromPropertyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .group("property.group")
                    .name("instant")
            )
            .jar(
                factory.jarBuilder("instant", path)
                    .group("property.group")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("group.value", "property.group")
                .plugin(
                    "${group.value}",
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
    void givenInterpolatedDependencyGroup_whenConstructToStage_thenDependencyWithGroupFromPropertyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .group("property.group")
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .group("property.group")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("group.value", "property.group")
                .plugin("dependencies")
                .dependency(
                    "${group.value}",
                    "dependency",
                    "1.0.0",
                    DependencyScope.IMPLEMENTATION
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .isEqualTo("property.group-dependency-1.0.0");
    }

    @Test
    void givenInterpolatedPreferenceGroup_whenConstructToStage_thenPreferenceWithGroupFromPropertyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .group("property.group")
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
                    .group("property.group")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("group.value", "property.group")
                .preference("${group.value}", "dependency", "1.0.0")
                .plugin("dependencies")
                .dependency(
                    "property.group",
                    "dependency",
                    null,
                    DependencyScope.IMPLEMENTATION
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .isEqualTo("property.group-dependency-1.0.0");
    }

    @Test
    void givenInterpolatedPreferenceInclusionGroup_whenConstructToStage_thenPreferenceInclusionWithGroupFromPropertyIsUsed(
        @TempDir Path path,
        ConveyorModule module,
        BuilderFactory factory
    ) throws Exception {
        factory.repositoryBuilder(path)
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependencies")
            )
            .jar(
                factory.jarBuilder("dependencies", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .name("dependency")
            )
            .jar(
                factory.jarBuilder("dependency", path)
            )
            .schematicDefinition(
                factory.schematicDefinitionBuilder()
                    .group("property.group")
                    .name("bom")
                    .preference("group", "dependency", "1.0.0")
            )
            .install(path);

        module.construct(
            factory.schematicDefinitionBuilder()
                .repository(path)
                .property("group.value", "property.group")
                .preferenceInclusion("${group.value}", "bom", "1.0.0")
                .plugin("dependencies")
                .dependency(
                    "group",
                    "dependency",
                    null,
                    DependencyScope.IMPLEMENTATION
                )
                .conveyorJson(path),
            List.of(Stage.COMPILE)
        );

        assertThat(path.resolve("dependencies"))
            .content()
            .isEqualTo("group-dependency-1.0.0");
    }
}
