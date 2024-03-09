package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorProperties;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class Properties {

    private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final ImmutableMap<String, String> map;

    Properties(ImmutableMap<String, String> map) {
        this.map = map;
    }

    Properties() {
        this(new ImmutableMap<>());
    }

    ConveyorProperties conveyorProperties() {
        return new ConveyorProperties(
            map.stream()
                .filter(entry -> !entry.getValue().isBlank())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> interpolated(entry.getValue()))),
            SchematicPropertyKey.NAME.fullName(),
            SchematicPropertyKey.DISCOVERY_DIRECTORY.fullName(),
            SchematicPropertyKey.CONSTRUCTION_DIRECTORY.fullName()
        );
    }

    String interpolated(String value) {
        return INTERPOLATION_PATTERN.matcher(value)
            .results()
            .reduce(
                value,
                (current, matchResult) ->
                    current.replace(
                        matchResult.group(),
                        map.value(matchResult.group(1))
                            .map(this::interpolated)
                            .orElseThrow()
                    ),
                new PickSecond<>()
            );
    }

    Properties override(Properties base) {
        return new Properties(base.map.withAll(map));
    }
}
