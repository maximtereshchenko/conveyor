package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

record ProjectDefinition(String name, int version, Path repository, Collection<PluginDefinition> plugins) {

    ProjectDefinition {
        repository = Objects.requireNonNullElse(repository, Paths.get(""));
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
