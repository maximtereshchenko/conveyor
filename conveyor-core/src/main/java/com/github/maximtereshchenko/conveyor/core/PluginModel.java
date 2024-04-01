package com.github.maximtereshchenko.conveyor.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

record PluginModel(
    Id id,
    Optional<String> version,
    Map<String, String> configuration
) implements ArtifactModel {

    PluginModel override(PluginModel base) {
        var copy = new HashMap<>(base.configuration());
        copy.putAll(configuration);
        return new PluginModel(id, version.or(base::version), copy);
    }
}
