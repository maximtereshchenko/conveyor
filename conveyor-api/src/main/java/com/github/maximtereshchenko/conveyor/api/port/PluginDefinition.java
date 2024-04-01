package com.github.maximtereshchenko.conveyor.api.port;

import java.util.Map;
import java.util.Objects;

public record PluginDefinition(String name, int version, Map<String, String> configuration)
    implements ArtifactDefinition {

    public PluginDefinition {
        configuration = Map.copyOf(Objects.requireNonNullElse(configuration, Map.of()));
    }
}
