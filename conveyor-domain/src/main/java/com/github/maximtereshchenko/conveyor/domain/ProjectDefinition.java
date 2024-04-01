package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

record ProjectDefinition(String name, int version, Path repository, Collection<PluginDefinition> plugins) {

    Map<String, String> pluginConfiguration(String name) {
        return plugins.stream()
            .filter(pluginDefinition -> pluginDefinition.name().equals(name))
            .map(PluginDefinition::configuration)
            .findAny()
            .orElseThrow();
    }
}
