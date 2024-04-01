package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Objects;

public record DependencyDefinition(String name, int version, DependencyScope scope)
    implements ArtifactDefinition {

    public DependencyDefinition {
        scope = Objects.requireNonNullElse(scope, DependencyScope.IMPLEMENTATION);
    }
}
