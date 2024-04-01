package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.stream.Stream;

final class SchematicProperties {

    private final ImmutableMap<SchematicPropertyKey, String> map;

    private SchematicProperties(ImmutableMap<SchematicPropertyKey, String> map) {
        this.map = map;
    }

    SchematicProperties() {
        this(new ImmutableMap<>());
    }

    static SchematicProperties from(ImmutableMap<String, String> map) {
        return new SchematicProperties(
            Stream.of(SchematicPropertyKey.values())
                .reduce(
                    new ImmutableMap<>(),
                    (schematicProperties, key) ->
                        map.value(key.fullName())
                            .map(value -> schematicProperties.with(key, value))
                            .orElse(schematicProperties),
                    new PickSecond<>()
                )
        );
    }

    SchematicProperties override(SchematicProperties base) {
        return new SchematicProperties(base.map.withAll(map));
    }

    Path discoveryDirectory(Path schematicDefinition) {
        var schematicDefinitionDirectory = schematicDefinition.getParent();
        return map.value(SchematicPropertyKey.DISCOVERY_DIRECTORY)
            .map(schematicDefinitionDirectory::resolve)
            .map(Path::normalize)
            .orElse(schematicDefinitionDirectory);
    }

    Properties properties(String name, Path schematicDefinition) {
        var discoveryDirectory = discoveryDirectory(schematicDefinition);
        return new Properties(
            new ImmutableMap<String, String>()
                .with(SchematicPropertyKey.NAME.fullName(), name)
                .with(SchematicPropertyKey.DISCOVERY_DIRECTORY.fullName(), discoveryDirectory.toString())
                .with(
                    SchematicPropertyKey.CONSTRUCTION_DIRECTORY.fullName(),
                    discoveryDirectory.resolve(
                            map.value(SchematicPropertyKey.CONSTRUCTION_DIRECTORY)
                                .orElse(".conveyor")
                        )
                        .normalize()
                        .toString()
                )
        );
    }
}
