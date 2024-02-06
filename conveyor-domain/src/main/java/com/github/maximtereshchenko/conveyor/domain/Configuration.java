package com.github.maximtereshchenko.conveyor.domain;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class Configuration {

    private final Properties properties;
    private final ImmutableMap<String, String> defined;
    private final Pattern interpolationPattern;

    Configuration(Properties properties, ImmutableMap<String, String> defined) {
        this.properties = properties;
        this.defined = defined;
        this.interpolationPattern = Pattern.compile("\\$\\{([^}]+)}");
    }

    Map<String, String> interpolated() {
        return defined.stream()
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toMap(Map.Entry::getKey, this::interpolated),
                    Map::copyOf
                )
            );
    }

    Configuration override(Configuration configuration) {
        return new Configuration(configuration.properties, defined.withAll(configuration.defined));
    }

    private String interpolated(Map.Entry<String, String> entry) {
        return interpolationPattern.matcher(entry.getValue())
            .results()
            .reduce(
                entry.getValue(),
                (current, matchResult) ->
                    current.replace(matchResult.group(), properties.value(matchResult.group(1))),
                new PickSecond<>()
            );
    }
}
