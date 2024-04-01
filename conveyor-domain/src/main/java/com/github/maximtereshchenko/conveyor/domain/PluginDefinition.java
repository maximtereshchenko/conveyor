package com.github.maximtereshchenko.conveyor.domain;

import java.util.Map;
import java.util.Objects;

record PluginDefinition(String name, int version, Map<String, String> configuration)
    implements VersionedArtifactDefinition {

    PluginDefinition {
        configuration = Objects.requireNonNullElse(configuration, Map.of());
    }
}
