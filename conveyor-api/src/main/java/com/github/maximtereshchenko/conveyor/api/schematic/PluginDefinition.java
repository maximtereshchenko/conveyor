package com.github.maximtereshchenko.conveyor.api.schematic;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record PluginDefinition(
    String group,
    String name,
    Optional<String> version,
    Map<String, String> configuration
) {

    public PluginDefinition {
        Objects.requireNonNull(group);
        Objects.requireNonNull(name);
        version = Objects.requireNonNullElse(version, Optional.empty());
        configuration = Map.copyOf(Objects.requireNonNullElse(configuration, Map.of()));
    }
}
