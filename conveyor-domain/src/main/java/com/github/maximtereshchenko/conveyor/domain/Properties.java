package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorProperties;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class Properties {

    private static final String SCHEMATIC_NAME_KEY = "conveyor.schematic.name";
    private static final String CONVEYOR_DISCOVERY_DIRECTORY_KEY = "conveyor.discovery.directory";
    private static final String CONVEYOR_CONSTRUCTION_DIRECTORY_KEY = "conveyor.construction.directory";
    private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final ImmutableMap<String, String> map;

    Properties(ImmutableMap<String, String> map) {
        this.map = map;
    }

    Properties() {
        this(new ImmutableMap<>());
    }

    Properties withDefaults(String schematicName, Path path) {
        var schematicDefinitionDirectory = path.getParent();
        var discoveryDirectory = map.value(CONVEYOR_DISCOVERY_DIRECTORY_KEY)
            .map(schematicDefinitionDirectory::resolve)
            .map(Path::normalize)
            .orElse(schematicDefinitionDirectory);
        return new Properties(
            map.with(SCHEMATIC_NAME_KEY, schematicName)
                .with(CONVEYOR_DISCOVERY_DIRECTORY_KEY, discoveryDirectory.toString())
                .with(
                    CONVEYOR_CONSTRUCTION_DIRECTORY_KEY,
                    discoveryDirectory.resolve(
                            map.value(CONVEYOR_CONSTRUCTION_DIRECTORY_KEY)
                                .orElse(".conveyor")
                        )
                        .normalize()
                        .toString()
                )
        );
    }

    ConveyorProperties conveyorProperties() {
        return new ConveyorProperties(
            map.stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
            SCHEMATIC_NAME_KEY,
            CONVEYOR_DISCOVERY_DIRECTORY_KEY,
            CONVEYOR_CONSTRUCTION_DIRECTORY_KEY
        );
    }

    String interpolated(String value) {
        return INTERPOLATION_PATTERN.matcher(value)
            .results()
            .reduce(
                value,
                (current, matchResult) ->
                    current.replace(matchResult.group(), map.value(matchResult.group(1)).orElseThrow()),
                new PickSecond<>()
            );
    }

    Properties override(Properties base) {
        return new Properties(base.map.withAll(map));
    }
}
