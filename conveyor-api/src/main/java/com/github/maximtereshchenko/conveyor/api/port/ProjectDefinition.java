package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ProjectDefinition(
    String name,
    int version,
    ParentDefinition parent,
    Path repository,
    Map<String, String> properties,
    Collection<PluginDefinition> plugins,
    Collection<DependencyDefinition> dependencies
) implements ArtifactDefinition {

    public ProjectDefinition {
        parent = Objects.requireNonNullElse(parent, new NoExplicitParent());
        repository = Objects.requireNonNullElse(repository, Paths.get(""));
        properties = Map.copyOf(Objects.requireNonNullElse(properties, Map.of()));
        plugins = List.copyOf(Objects.requireNonNullElse(plugins, List.of()));
        dependencies = List.copyOf(Objects.requireNonNullElse(dependencies, List.of()));
    }
}
