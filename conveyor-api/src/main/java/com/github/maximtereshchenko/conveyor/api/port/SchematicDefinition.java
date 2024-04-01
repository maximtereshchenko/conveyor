package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SchematicDefinition(
    String name,
    int version,
    TemplateForSchematicDefinition template,
    List<Path> inclusions,
    Collection<RepositoryDefinition> repositories,
    Map<String, String> properties,
    PreferencesDefinition preferences,
    Collection<PluginDefinition> plugins,
    Collection<SchematicDependencyDefinition> dependencies
) {

    public SchematicDefinition {
        template = Objects.requireNonNullElse(template, new NoExplicitlyDefinedTemplate());
        inclusions = List.copyOf(Objects.requireNonNullElse(inclusions, List.of()));
        repositories = List.copyOf(Objects.requireNonNullElse(repositories, List.of()));
        properties = Map.copyOf(Objects.requireNonNullElse(properties, Map.of()));
        preferences = Objects.requireNonNullElse(preferences, new PreferencesDefinition());
        plugins = List.copyOf(Objects.requireNonNullElse(plugins, List.of()));
        dependencies = List.copyOf(Objects.requireNonNullElse(dependencies, List.of()));
    }
}
