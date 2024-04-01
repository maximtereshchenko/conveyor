package com.github.maximtereshchenko.conveyor.domain;

import java.util.Map;
import java.util.stream.Collectors;

final class Configuration {

    private static final String ENABLED_KEY = "enabled";

    private final ImmutableMap<String, String> map;

    private Configuration(ImmutableMap<String, String> map) {
        this.map = map;
    }

    static Configuration from(Map<String, String> map) {
        return new Configuration(new ImmutableMap<>(map));
    }

    Configuration override(Configuration base) {
        return new Configuration(base.map.withAll(map));
    }

    boolean isEnabled() {
        return map.value(ENABLED_KEY)
            .map(Boolean::valueOf)
            .orElse(Boolean.TRUE);
    }

    Map<String, String> interpolated(Properties properties) {
        return map.computeIfAbsent(ENABLED_KEY, () -> "true")
            .stream()
            .filter(entry -> !entry.getValue().isBlank())
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> properties.interpolated(entry.getValue())));
    }
}
