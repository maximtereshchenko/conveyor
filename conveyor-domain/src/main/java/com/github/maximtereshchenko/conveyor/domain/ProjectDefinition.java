package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

record ProjectDefinition(
    String name,
    int version,
    Path repository,
    Map<String, String> properties,
    Collection<PluginDefinition> plugins
) implements ArtifactDefinition {

    ProjectDefinition {
        repository = Objects.requireNonNullElse(repository, Paths.get(""));
        properties = Objects.requireNonNullElse(properties, Map.of());
        plugins = Objects.requireNonNullElse(plugins, List.of());
    }

    Map<String, String> pluginConfiguration(String name) {
        return plugins.stream()
            .filter(pluginDefinition -> pluginDefinition.name().equals(name))
            .map(PluginDefinition::configuration)
            .findAny()
            .orElseThrow();
    }
}
