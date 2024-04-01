package com.github.maximtereshchenko.conveyor.api.port;

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
        configuration = Map.copyOf(Objects.requireNonNullElse(configuration, Map.of()));
    }
}
