package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorProperties;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class Properties {

    private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final Map<String, String> all;

    Properties(Map<String, String> all) {
        this.all = all;
    }

    ConveyorProperties conveyorProperties() {
        return new ConveyorProperties(
            all.entrySet()
                .stream()
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
                        interpolated(all.get(matchResult.group(1)))
                    ),
                (a, b) -> a
            );
    }
}
