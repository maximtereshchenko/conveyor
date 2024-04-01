package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Objects;
import java.util.Optional;

public record DependencyDefinition(
    String group,
    String name,
    Optional<String> version,
    Optional<DependencyScope> scope
) {

    public DependencyDefinition {
        Objects.requireNonNull(group);
        Objects.requireNonNull(name);
        version = Objects.requireNonNullElse(version, Optional.empty());
        scope = Objects.requireNonNullElse(scope, Optional.empty());
    }
}
