package com.github.maximtereshchenko.conveyor.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

record PluginModel(
    IdModel idModel,
    Optional<String> version,
    Map<String, String> configuration
) implements ArtifactModel {

    @Override
    public Set<Id> exclusions() {
        return Set.of();
    }

    PluginModel override(PluginModel base) {
        var copy = new HashMap<>(base.configuration());
        copy.putAll(configuration);
        return new PluginModel(idModel, version.or(base::version), copy);
    }
}
