package com.github.maximtereshchenko.conveyor.api.schematic;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SchematicDefinition(
    String group,
    String name,
    String version,
    TemplateDefinition template,
    List<Path> inclusions,
    List<RepositoryDefinition> repositories,
    Map<String, String> properties,
    PreferencesDefinition preferences,
    List<PluginDefinition> plugins,
    List<DependencyDefinition> dependencies
) {

    public SchematicDefinition {
        Objects.requireNonNull(group);
        Objects.requireNonNull(name);
        Objects.requireNonNull(version);
        template = Objects.requireNonNullElse(template, new NoTemplateDefinition());
        inclusions = List.copyOf(Objects.requireNonNullElse(inclusions, List.of()));
        repositories = List.copyOf(Objects.requireNonNullElse(repositories, List.of()));
        properties = Map.copyOf(Objects.requireNonNullElse(properties, Map.of()));
        preferences = Objects.requireNonNullElse(preferences, new PreferencesDefinition());
        plugins = List.copyOf(Objects.requireNonNullElse(plugins, List.of()));
        dependencies = List.copyOf(Objects.requireNonNullElse(dependencies, List.of()));
    }
}
