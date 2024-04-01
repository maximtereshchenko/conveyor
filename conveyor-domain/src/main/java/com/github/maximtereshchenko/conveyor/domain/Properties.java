package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorProperties;

import java.nio.file.Path;
import java.util.Map;

final class Properties {

    private final ImmutableMap<String, String> map;

    Properties(ImmutableMap<String, String> map) {
        this.map = map;
    }

    Properties(Map<String, String> map) {
        this(new ImmutableMap<>(map));
    }

    Properties() {
        this(new ImmutableMap<>());
    }

    ConveyorProperties conveyorProperties(Path path, String schematicName) {
        var schematicNameKey = "conveyor.schematic.name";
        var discoveryDirectoryKey = "conveyor.discovery.directory";
        var constructionDirectoryKey = "conveyor.construction.directory";
        var schematicDefinitionDirectory = path.getParent().normalize();
        var discoveryDirectory = map.value(discoveryDirectoryKey)
            .map(schematicDefinitionDirectory::resolve)
            .map(Path::normalize)
            .orElse(schematicDefinitionDirectory);
        return new ConveyorProperties(
            map.with(schematicNameKey, schematicName)
                .with(discoveryDirectoryKey, discoveryDirectory.toString())
                .with(
                    constructionDirectoryKey,
                    discoveryDirectory.resolve(
                            map.value(constructionDirectoryKey)
                                .orElse(".conveyor")
                        )
                        .normalize()
                        .toString()
                )
                .mutable(),
            schematicNameKey,
            discoveryDirectoryKey,
            constructionDirectoryKey
        );
    }

    String value(String key) {
        return map.value(key).orElse("");
    }

    Properties override(Properties properties) {
        return new Properties(map.withAll(properties.map));
    }
}
