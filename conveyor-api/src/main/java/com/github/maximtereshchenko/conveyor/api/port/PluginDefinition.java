package com.github.maximtereshchenko.conveyor.api.port;

import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

public record PluginDefinition(String name, OptionalInt version, Map<String, String> configuration) {

    public PluginDefinition {
        configuration = Map.copyOf(Objects.requireNonNullElse(configuration, Map.of()));
    }
}
