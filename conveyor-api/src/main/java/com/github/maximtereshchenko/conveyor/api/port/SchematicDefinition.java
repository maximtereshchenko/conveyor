package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SchematicDefinition(
    String name,
    int version,
    TemplateDefinition template,
    Collection<Path> inclusions,
    Path repository,
    Map<String, String> properties,
    Collection<PluginDefinition> plugins,
    Collection<DependencyDefinition> dependencies
) {

    public SchematicDefinition {
        template = Objects.requireNonNullElse(template, new NoExplicitTemplate());
        inclusions = List.copyOf(Objects.requireNonNullElse(inclusions, List.of()));
        repository = Objects.requireNonNullElse(repository, Paths.get(""));
        properties = Map.copyOf(Objects.requireNonNullElse(properties, Map.of()));
        plugins = List.copyOf(Objects.requireNonNullElse(plugins, List.of()));
        dependencies = List.copyOf(Objects.requireNonNullElse(dependencies, List.of()));
    }
}
