package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;
import java.util.*;

public record SchematicDefinition(
    String name,
    int version,
    TemplateDefinition template,
    List<Path> inclusions,
    Optional<Path> repository,
    Map<String, String> properties,
    Collection<PluginDefinition> plugins,
    Collection<DependencyDefinition> dependencies
) {

    public SchematicDefinition {
        template = Objects.requireNonNullElse(template, new NoExplicitTemplate());
        inclusions = List.copyOf(Objects.requireNonNullElse(inclusions, List.of()));
        properties = Map.copyOf(Objects.requireNonNullElse(properties, Map.of()));
        plugins = List.copyOf(Objects.requireNonNullElse(plugins, List.of()));
        dependencies = List.copyOf(Objects.requireNonNullElse(dependencies, List.of()));
    }
}
