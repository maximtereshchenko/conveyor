package com.github.maximtereshchenko.conveyor.api.schematic;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DependencyDefinition(
    String group,
    String name,
    Optional<String> version,
    Optional<DependencyScope> scope,
    List<ExclusionDefinition> exclusions
) {

    public DependencyDefinition {
        Objects.requireNonNull(group);
        Objects.requireNonNull(name);
        version = Objects.requireNonNullElse(version, Optional.empty());
        scope = Objects.requireNonNullElse(scope, Optional.empty());
        exclusions = List.copyOf(Objects.requireNonNullElse(exclusions, List.of()));
    }
}
