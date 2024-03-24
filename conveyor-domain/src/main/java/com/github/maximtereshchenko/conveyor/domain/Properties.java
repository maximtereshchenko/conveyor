package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
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
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> interpolated(entry.getValue())
                    )
                ),
            ConveyorPropertyKey.SCHEMATIC_NAME.fullName(),
            ConveyorPropertyKey.DISCOVERY_DIRECTORY.fullName(),
            ConveyorPropertyKey.CONSTRUCTION_DIRECTORY.fullName()
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

    Path remoteRepositoryCacheDirectory() {
        return Paths.get(all.get(ConveyorPropertyKey.REMOTE_REPOSITORY_CACHE_DIRECTORY.fullName()));
    }
}
