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
    Path repository,
    Map<String, String> properties,
    Collection<PluginDefinition> plugins,
    Collection<ProjectDependencyDefinition> dependencies
) implements ArtifactDefinition {

    public ProjectDefinition {
        repository = Objects.requireNonNullElse(repository, Paths.get(""));
        properties = Objects.requireNonNullElse(properties, Map.of());
        plugins = Objects.requireNonNullElse(plugins, List.of());
        dependencies = Objects.requireNonNullElse(dependencies, List.of());
    }
}
