package com.github.maximtereshchenko.conveyor.domain;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class InterpolationService {

    private final Pattern interpolationPattern = Pattern.compile("\\$\\{([^}]+)}");

    Map<String, String> interpolate(Map<String, String> source, Map<String, String> target) {
        return target.entrySet()
            .stream()
            .map(entry -> Map.entry(entry.getKey(), interpolate(entry.getValue(), source)))
            .collect(Collectors.collectingAndThen(Collectors.toMap(Entry::getKey, Entry::getValue), Map::copyOf));
    }

    private String interpolate(String value, Map<String, String> properties) {
        return interpolationPattern.matcher(value)
            .results()
            .reduce(
                value,
                (current, matchResult) ->
                    current.replace(matchResult.group(), properties.getOrDefault(matchResult.group(1), "")),
                (first, second) -> first
            );
    }
}
