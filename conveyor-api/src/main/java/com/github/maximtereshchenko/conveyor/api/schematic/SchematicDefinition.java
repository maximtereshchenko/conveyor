package com.github.maximtereshchenko.conveyor.api.schematic;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record SchematicDefinition(
    Optional<String> group,
    String name,
    Optional<String> version,
    TemplateDefinition template,
    List<Path> inclusions,
    List<RepositoryDefinition> repositories,
    Map<String, String> properties,
    PreferencesDefinition preferences,
    List<PluginDefinition> plugins,
    List<DependencyDefinition> dependencies
) {

    public SchematicDefinition {
        group = Objects.requireNonNullElse(group, Optional.empty());
        Objects.requireNonNull(name);
        version = Objects.requireNonNullElse(version, Optional.empty());
        template = Objects.requireNonNullElse(template, new NoTemplateDefinition());
        inclusions = List.copyOf(Objects.requireNonNullElse(inclusions, List.of()));
        repositories = List.copyOf(Objects.requireNonNullElse(repositories, List.of()));
        properties = Map.copyOf(Objects.requireNonNullElse(properties, Map.of()));
        preferences = Objects.requireNonNullElse(preferences, new PreferencesDefinition());
        plugins = List.copyOf(Objects.requireNonNullElse(plugins, List.of()));
        dependencies = List.copyOf(Objects.requireNonNullElse(dependencies, List.of()));
    }
}
