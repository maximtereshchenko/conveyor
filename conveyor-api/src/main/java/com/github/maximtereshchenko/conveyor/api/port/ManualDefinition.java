package com.github.maximtereshchenko.conveyor.api.port;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ManualDefinition(
    String group,
    String name,
    String version,
    TemplateForManualDefinition template,
    Map<String, String> properties,
    PreferencesDefinition preferences,
    Collection<PluginDefinition> plugins,
    Collection<ManualDependencyDefinition> dependencies
) {

    public ManualDefinition {
        template = Objects.requireNonNullElse(template, new NoTemplate());
        properties = Map.copyOf(Objects.requireNonNullElse(properties, Map.of()));
        preferences = Objects.requireNonNullElse(preferences, new PreferencesDefinition());
        plugins = List.copyOf(Objects.requireNonNullElse(plugins, List.of()));
        dependencies = List.copyOf(Objects.requireNonNullElse(dependencies, List.of()));
    }
}
